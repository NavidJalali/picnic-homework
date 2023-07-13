package tech.picnic.assignment.impl.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
final class DeliveryResultTest {
  @Test
  void deliveryStatusIsCancelledIfNoDeliveredPresent() {
    Optional<DeliveryResult> result = DeliveryResult.fromOrders(
      List.of(
        new Order(
          new OrderId("1"),
          OrderStatus.CANCELLED,
          new Delivery(
            new DeliveryId("1"),
            new DeliveryTime(OffsetDateTime.now())
          ),
          Optional.empty()
        ),
        new Order(
          new OrderId("2"),
          OrderStatus.CANCELLED,
          new Delivery(
            new DeliveryId("1"),
            new DeliveryTime(OffsetDateTime.now())
          ),
          Optional.of(new Amount(1L))
        )
      )
    );

    assertSame(result.get().deliveryStatus(), DeliveryStatus.CANCELLED);
  }

  @Test
  void deliveryStatusDeliveredIfAtLeastOneDeliveredPresent(){
    Optional<DeliveryResult> result = DeliveryResult.fromOrders(
      List.of(
        new Order(
          new OrderId("1"),
          OrderStatus.DELIVERED,
          new Delivery(
            new DeliveryId("1"),
            new DeliveryTime(OffsetDateTime.now())
          ),
          Optional.of(new Amount(1L))
        ),
        new Order(
          new OrderId("2"),
          OrderStatus.CANCELLED,
          new Delivery(
            new DeliveryId("1"),
            new DeliveryTime(OffsetDateTime.now())
          ),
          Optional.empty()
        )
      )
    );

    assertSame(result.get().deliveryStatus(), DeliveryStatus.DELIVERED);
  }

  @Test
  void resultsAreSortedByDeliveryTimeAndThenDeliveryId() {
    OffsetDateTime now = OffsetDateTime.now();
    List<DeliveryResult> results =
      Stream.of(
        new DeliveryResult(
          new DeliveryId("1"),
          new DeliveryTime(now),
          DeliveryStatus.DELIVERED,
          List.of(),
          Optional.empty()
        ),
        new DeliveryResult(
          new DeliveryId("2"),
          new DeliveryTime(now.minusDays(1)),
          DeliveryStatus.DELIVERED,
          List.of(),
          Optional.empty()
        ),
        new DeliveryResult(
          new DeliveryId("3"),
          new DeliveryTime(now),
          DeliveryStatus.DELIVERED,
          List.of(),
          Optional.empty()
        )
      )
          .sorted(DeliveryResult.comparator)
          .toList();

    assertEquals(results.get(0).deliveryId().value(), "2");
    assertEquals(results.get(1).deliveryId().value(), "1");
    assertEquals(results.get(2).deliveryId().value(), "3");
  }
}
