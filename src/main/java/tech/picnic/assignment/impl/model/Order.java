package tech.picnic.assignment.impl.model;


import java.util.Optional;

public record Order(
  OrderId orderId,
  OrderStatus orderStatus,
  Delivery delivery,
  Optional<Amount> amount
) {
  @Override
  public String toString() {
    return orderId.value();
  }
}
