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

@JsonSerialize(using = Amount.AmountSerializer.class)
@JsonDeserialize(using = Amount.AmountDeserializer.class)
public record Amount(Long value) {
  public static class AmountSerializer extends JsonSerializer<Amount> {
    @Override
    public void serialize(Amount amount, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
      gen.writeNumber(amount.value());
    }
  }

  public static class AmountDeserializer extends JsonDeserializer<Amount> {
    @Override
    public Amount deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      return new Amount(p.getValueAsLong());
    }
  }

  public Amount plus(Amount other) {
    return new Amount(this.value + other.value);
  }
}
