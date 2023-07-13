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

@JsonSerialize(using = DeliveryId.DeliveryIdSerializer.class)
@JsonDeserialize(using = DeliveryId.DeliveryIdDeserializer.class)
public record DeliveryId(String value) {
  public static class DeliveryIdSerializer extends JsonSerializer<DeliveryId> {
    @Override
    public void serialize(DeliveryId deliveryId, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
      gen.writeString(deliveryId.value());
    }
  }

  public static class DeliveryIdDeserializer extends JsonDeserializer<DeliveryId> {
    @Override
    public DeliveryId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      return new DeliveryId(p.getValueAsString());
    }
  }
}
