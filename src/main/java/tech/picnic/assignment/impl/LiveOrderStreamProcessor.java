package tech.picnic.assignment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import tech.picnic.assignment.api.OrderStreamProcessor;
import tech.picnic.assignment.impl.model.DeliveryResult;
import tech.picnic.assignment.impl.model.Order;

import javax.annotation.WillNotClose;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LiveOrderStreamProcessor implements OrderStreamProcessor {

  private final ObjectMapper objectMapper;
  private final int maxOrders;
  private final Duration maxDuration;

  public LiveOrderStreamProcessor(int maxOrders, Duration maxDuration) {
    this.objectMapper = ObjectMapperFactory.createObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    objectMapper.registerModule(new Jdk8Module());
    this.maxOrders = maxOrders;
    this.maxDuration = maxDuration;
  }

  /**
   * Based on the signature (void + throws IOException), this method will have to block.
   * Ensure to use the right threadpool when calling this method.
   */
  @Override
  public void process(InputStream source, OutputStream sink) throws IOException {
    try {
      processOrdersAsync(source, sink).get();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof IOException)
        throw (IOException) e.getCause();
      else
        throw new RuntimeException(e.getCause());
    }
  }

  private CompletableFuture<Void> processOrdersAsync(
    InputStream source,
    OutputStream sink
  ) {
    return aggregateAsyncWithin(
      source,
      this::parseLine,
      maxOrders,
      maxDuration
    )
      .thenApply(this::processOrders)
      .thenApply(Stream::toList)
      .thenApply(this::asJsonBytes)
      .thenAccept(bytes -> this.writeToSink(sink, bytes));
  }

  private Stream<DeliveryResult> processOrders(Queue<Order> orders) {
    return
      orders
        .stream()
        .filter(order -> order.orderStatus().isDeliveredOrCancelled())
        .collect(Collectors.groupingBy(order -> order.delivery().deliveryId()))
        .values()
        .stream()
        .map(DeliveryResult::fromOrders)
        .flatMap(Optional::stream)
        .sorted(DeliveryResult.comparator);
  }

  private Optional<Order> parseLine(String line) {
    try {
      return Optional.of(objectMapper.readValue(line, Order.class));
    } catch (JsonProcessingException e) {
      return Optional.empty();
    }
  }

  private byte[] asJsonBytes(List<DeliveryResult> results) {
    try {
      return objectMapper.writeValueAsString(results).getBytes(StandardCharsets.UTF_8);
    } catch (JsonProcessingException e) {
      throw new CompletionException(e);
    }
  }

  /**
   * Read a line from the reader but block until the specified maxTime has passed otherwise return empty
   */
  private Optional<String> readLineBlockingUntil(
    BufferedReader reader,
    Duration maxTime
  ) throws IOException {
    CompletableFuture<Optional<String>> readLine = CompletableFuture.supplyAsync(() -> {
      try {
        return Optional.ofNullable(reader.readLine());
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    });

    try {
      return readLine.get(maxTime.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | TimeoutException e) {
      return Optional.empty();
    } catch (ExecutionException e) {
      throw new IOException(e);
    }
  }

  /**
   * Calculate the remaining time for execution based on the start time and the maxDuration
   */
  private Optional<Long> remainingTime(
    long startTime,
    Duration maxDuration
  ) {
    long elapsed = System.currentTimeMillis() - startTime;
    if (elapsed > maxDuration.toMillis())
      return Optional.empty();
    else
      return Optional.of(maxDuration.toMillis() - elapsed);
  }

  /**
   * Aggregate items from the source stream until either the maxItems or maxTime is reached
   */
  private <A> CompletableFuture<Queue<A>> aggregateAsyncWithin(
    @WillNotClose InputStream source,
    Function<String, Optional<A>> parse,
    int maxItems,
    Duration maxTime
  ) {
    return CompletableFuture.supplyAsync(() -> {
      ConcurrentLinkedQueue<A> offeredItems = new ConcurrentLinkedQueue<>();
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(source));
        final long startTime = System.currentTimeMillis();
        Optional<Long> maybeRemainingTime;
        while (
          offeredItems.size() < maxItems &&
            (maybeRemainingTime = remainingTime(startTime, maxTime)).isPresent()
        ) {
          long remainingTime = maybeRemainingTime.get();
          Optional<String> maybeLine =
            readLineBlockingUntil(reader, Duration.ofMillis(remainingTime));

          if (maybeLine.isEmpty()) {
            break;
          }

          maybeLine.flatMap(parse).ifPresent(offeredItems::offer);
        }
        return offeredItems;
      } catch (IOException e) {
        throw new CompletionException(e);
      }
    });
  }

  private void writeToSink(OutputStream sink, byte[] bytes) {
    try {
      sink.write(bytes);
    } catch (IOException e) {
      throw new CompletionException(e);
    }
  }
}
