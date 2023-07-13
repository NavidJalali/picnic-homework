package tech.picnic.assignment.impl.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize(using = OrderId.OrderIdSerializer.class)
@JsonDeserialize(using = OrderId.OrderIdDeserializer.class)
public record OrderId(String value) {
  public static class OrderIdSerializer extends JsonSerializer<OrderId> {
    @Override
    public void serialize(OrderId orderId, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
      gen.writeString(orderId.value());
    }
  }

  public static class OrderIdDeserializer extends JsonDeserializer<OrderId> {
    @Override
    public OrderId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      return new OrderId(p.getValueAsString());
    }
  }
}
