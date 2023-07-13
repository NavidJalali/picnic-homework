package tech.picnic.assignment.impl.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record DeliveryResult(
  DeliveryId deliveryId,
  DeliveryTime deliveryTime,
  DeliveryStatus deliveryStatus,
  List<DeliveryOrder> orders,
  Optional<Amount> totalAmount
) {

  public static Comparator<DeliveryResult> comparator =
    new Comparator<>() {
      @Override
      public int compare(DeliveryResult u, DeliveryResult v) {
        int timeComparison = u.deliveryTime().value().compareTo(v.deliveryTime().value());
        if (timeComparison != 0) {
          return timeComparison;
        }
        return u.deliveryId().value().compareTo(v.deliveryId().value());
      }
    };

  public static Optional<DeliveryResult> fromOrders(List<Order> orders) {
    if (orders.isEmpty()) {
      return Optional.empty();
    }

    assert orders.stream().allMatch(
      order -> order.delivery().deliveryId().equals(orders.get(0).delivery().deliveryId())
    );

    DeliveryStatus deliveryStatus =
      orders.stream()
        .filter(order -> order.orderStatus() == OrderStatus.DELIVERED)
        .findAny()
        .map(order -> DeliveryStatus.DELIVERED)
        .orElseGet(() -> DeliveryStatus.CANCELLED);

    Optional<Amount> totalAmount =
      orders.stream()
        .filter(order -> order.orderStatus() == OrderStatus.DELIVERED)
        .map(Order::amount)
        .flatMap(Optional::stream)
        .reduce(Amount::plus);

    DeliveryId deliveryId = orders.get(0).delivery().deliveryId();

    DeliveryTime deliveryTime = orders.get(0).delivery().deliveryTime();

    List<DeliveryOrder> deliveryOrders =
      orders.stream()
        .map(order -> new DeliveryOrder(order.orderId(), order.amount()))
        .sorted(DeliveryOrder.comparator)
        .toList();

    return Optional.of(
      new DeliveryResult(
        deliveryId,
        deliveryTime,
        deliveryStatus,
        deliveryOrders,
        totalAmount
      )
    );
  }
}
