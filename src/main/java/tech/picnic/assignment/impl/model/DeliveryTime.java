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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@JsonSerialize(using = DeliveryTime.DeliveryTimeSerializer.class)
@JsonDeserialize(using = DeliveryTime.DeliveryTimeDeserializer.class)
public record DeliveryTime(OffsetDateTime value) {

  private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  public static class DeliveryTimeSerializer extends JsonSerializer<DeliveryTime> {
    @Override
    public void serialize(DeliveryTime deliveryTime, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
      gen.writeString(formatter.format(deliveryTime.value()));
    }
  }

  public static class DeliveryTimeDeserializer extends JsonDeserializer<DeliveryTime> {
    @Override
    public DeliveryTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      OffsetDateTime value = OffsetDateTime.parse(p.getValueAsString());
      return new DeliveryTime(value);
    }
  }
}
