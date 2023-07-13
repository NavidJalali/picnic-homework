package tech.picnic.assignment.impl.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliveryStatus {
  DELIVERED("delivered"),
  CANCELLED("cancelled");

  private final String value;

  DeliveryStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static DeliveryStatus fromValue(String value) {
    for (DeliveryStatus status : values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Invalid DeliveryStatus value: " + value);
  }

  public static DeliveryStatus fromOrderStatus(OrderStatus orderStatus) {
    return switch (orderStatus) {
      case DELIVERED -> DELIVERED;
      case CANCELLED -> CANCELLED;
      default -> throw new IllegalArgumentException("Invalid OrderStatus value: " + orderStatus);
    };
  }
}
