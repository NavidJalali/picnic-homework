package tech.picnic.assignment.impl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
  CREATED("created"),
  DELIVERED("delivered"),
  CANCELLED("cancelled");

  private final String value;

  OrderStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static OrderStatus fromValue(String value) {
    for (OrderStatus status : values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid OrderStatus value: " + value);
  }

  public boolean isDeliveredOrCancelled() {
    return this == DELIVERED || this == CANCELLED;
  }
}
