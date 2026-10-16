package service;

import domain.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderService {
    private final Map<Integer, Order> orders = new LinkedHashMap<>();
    private final List<Courier> couriers = new ArrayList<>();
    private final ProductCatalog catalog = new ProductCatalog();
    private int nextOrderNumber = 1;

    public OrderService() {
        couriers.add(new Courier(1, "Иван Петров", "+7-900-111-22-33"));
        couriers.add(new Courier(2, "Анна Смирнова", "+7-900-222-33-44"));
        couriers.add(new Courier(3, "Павел Орлов", "+7-900-333-44-55"));
    }

    public ProductCatalog getCatalog() {
        return catalog;
    }

    public Order createOrder(Customer customer, String deliveryAddress) {
        Order order = new Order(nextOrderNumber++, customer, deliveryAddress);
        orders.put(order.getNumber(), order);
        return order;
    }

    public void addItem(int orderNumber, int productId, int quantity) {
        Order order = getOrder(orderNumber);
        Product product = catalog.getById(productId);
        order.addItem(product, quantity);
    }

    public List<Order> getOrders() {
        return List.copyOf(orders.values());
    }

    public Order getOrder(int number) {
        Order order = orders.get(number);
        if (order == null) {
            throw new DeliveryException("Заказ №" + number + " не найден.");
        }
        return order;
    }

    public void selectDeliveryMethod(int orderNumber, DeliveryMethod method) {
        getOrder(orderNumber).selectDeliveryMethod(method);
    }

    public void assignCourier(int orderNumber, Courier courier) {
        getOrder(orderNumber).assignCourier(courier);
    }

    public void changeStatus(int orderNumber, OrderStatus newStatus) {
        Order order = getOrder(orderNumber);
        order.changeStatus(newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            order.releaseItems();
        }
        if (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED) {
            order.getCourier().ifPresent(Courier::markAvailable);
        }
    }

    public List<Courier> getAvailableCouriers() {
        return couriers.stream()
                .filter(Courier::isAvailable)
                .toList();
    }
}