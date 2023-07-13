package tech.picnic.assignment.impl.model;

import java.util.Comparator;
import java.util.Optional;

public record DeliveryOrder(
  OrderId orderId,
  Optional<Amount> amount
) {
  public static Comparator<DeliveryOrder> comparator =
    new Comparator<>() {
      @Override
      public int compare(DeliveryOrder u, DeliveryOrder v) {
        return v.orderId().value().compareTo(u.orderId().value());
      }
    };
}
