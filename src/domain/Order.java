package domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Order {
    private final int number;
    private final Customer customer;
    private final String deliveryAddress;
    private final List<OrderItem> items = new ArrayList<>();

    private DeliveryMethod deliveryMethod;
    private Courier courier;
    private OrderStatus status = OrderStatus.NEW;
    private BigDecimal itemsTotal = BigDecimal.ZERO;
    private BigDecimal deliveryCost = BigDecimal.ZERO;

    public Order(int number, Customer customer, String deliveryAddress) {
        if (number <= 0) {
            throw new DeliveryException("Номер заказа должен быть положительным.");
        }
        if (customer == null) {
            throw new DeliveryException("Клиент не может быть null.");
        }
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new DeliveryException("Адрес доставки не может быть пустым.");
        }

        this.number = number;
        this.customer = customer;
        this.deliveryAddress = deliveryAddress.trim();
    }

    public void checkCanAddItems() {
        if (status != OrderStatus.NEW && status != OrderStatus.CONFIRMED) {
            throw new DeliveryException(
                    "Позиции можно добавлять только в заказ в статусе \"Новый\" или \"Подтверждён\". "
                            + "Текущий статус: \"" + status.getTitle() + "\"."
            );
        }
    }

    public void checkCanSelectDeliveryMethod() {
        if (status != OrderStatus.NEW && status != OrderStatus.CONFIRMED) {
            throw new DeliveryException(
                    "Способ доставки можно выбрать только для заказа в статусе \"Новый\" или \"Подтверждён\". "
                            + "Текущий статус: \"" + status.getTitle() + "\"."
            );
        }
    }

    public void checkCanAssignCourier() {
        if (deliveryMethod == null) {
            throw new DeliveryException("Сначала выберите способ доставки.");
        }
        if (!deliveryMethod.requiresCourier()) {
            throw new DeliveryException("Для выбранного способа доставки курьер не требуется.");
        }
        if (status != OrderStatus.CONFIRMED) {
            throw new DeliveryException(
                    "Курьера можно назначить только для заказа в статусе \"Подтверждён\". "
                            + "Текущий статус: \"" + status.getTitle() + "\"."
            );
        }
        if (courier != null) {
            throw new DeliveryException("Курьер уже назначен на этот заказ.");
        }
    }

    public void addItem(Product product, int quantity) {
        checkCanAddItems();
        if (product == null) {
            throw new DeliveryException("Товар не может быть null.");
        }

        product.reserve(quantity);

        items.add(new OrderItem(product, quantity));
        recalculateItemsTotal();

        if (deliveryMethod != null) {
            deliveryCost = deliveryMethod.calculateCost(itemsTotal);
        }
    }

    public void releaseItems() {
        for (OrderItem item : items) {
            item.getProduct().release(item.getQuantity());
        }
    }

    private void recalculateItemsTotal() {
        itemsTotal = items.stream()
                .map(OrderItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void selectDeliveryMethod(DeliveryMethod method) {
        checkCanSelectDeliveryMethod();

        this.deliveryMethod = Objects.requireNonNull(method, "Способ доставки не может быть null.");
        this.deliveryCost = method.calculateCost(itemsTotal);

        if (status == OrderStatus.NEW) {
            changeStatus(OrderStatus.CONFIRMED);
        }
    }

    public void assignCourier(Courier courier) {
        checkCanAssignCourier();

        if (courier == null) {
            throw new DeliveryException("Курьер не может быть null.");
        }
        if (!courier.isAvailable()) {
            throw new DeliveryException("Курьер " + courier.getName() + " сейчас занят.");
        }

        this.courier = courier;
        courier.markBusy();
        changeStatus(OrderStatus.ASSIGNED);
    }

    public void changeStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new DeliveryException("Статус не может быть null.");
        }

        if (!status.canTransitionTo(newStatus)) {
            throw new DeliveryException(
                    "Нельзя перевести заказ из статуса \""
                            + status.getTitle()
                            + "\" в \""
                            + newStatus.getTitle()
                            + "\"."
            );
        }

        switch (newStatus) {
            case ASSIGNED -> {
                if (deliveryMethod == null || !deliveryMethod.requiresCourier()) {
                    throw new DeliveryException("Статус \"Курьер назначен\" доступен только для доставки курьером.");
                }
                if (courier == null) {
                    throw new DeliveryException("Сначала назначьте курьера.");
                }
            }
            case READY_FOR_PICKUP -> {
                if (deliveryMethod == null || deliveryMethod.requiresCourier()) {
                    throw new DeliveryException("Статус \"Готов к самовывозу\" доступен только для самовывоза.");
                }
            }
            case IN_DELIVERY -> {
                if (courier == null) {
                    throw new DeliveryException("Нельзя начать доставку без назначенного курьера.");
                }
            }
            default -> {
            }
        }

        this.status = newStatus;
    }

    public int getNumber() {
        return number;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Optional<DeliveryMethod> getDeliveryMethod() {
        return Optional.ofNullable(deliveryMethod);
    }

    public Optional<Courier> getCourier() {
        return Optional.ofNullable(courier);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getItemsTotal() {
        return itemsTotal;
    }

    public BigDecimal getDeliveryCost() {
        return deliveryCost;
    }

    public BigDecimal getTotalCost() {
        return itemsTotal.add(deliveryCost);
    }

    public String toShortString() {
        String methodName = deliveryMethod == null ? "способ не выбран" : deliveryMethod.getName();
        return String.format(
                "№%d | клиент: %s | статус: %s | доставка: %s | итого: %s",
                number,
                customer.getName(),
                status.getTitle(),
                methodName,
                getTotalCost()
        );
    }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Заказ №").append(number).append(System.lineSeparator());
        sb.append("Клиент: ").append(customer.getName())
                .append(", тел. ").append(customer.getPhone()).append(System.lineSeparator());
        sb.append("Адрес доставки: ").append(deliveryAddress).append(System.lineSeparator());
        sb.append("Статус: ").append(status.getTitle()).append(System.lineSeparator());

        sb.append("Позиции:").append(System.lineSeparator());
        if (items.isEmpty()) {
            sb.append("  нет").append(System.lineSeparator());
        } else {
            for (OrderItem item : items) {
                sb.append("  - ").append(item).append(System.lineSeparator());
            }
        }

        sb.append("Сумма позиций: ").append(itemsTotal).append(System.lineSeparator());

        if (deliveryMethod == null) {
            sb.append("Способ доставки: не выбран").append(System.lineSeparator());
        } else {
            sb.append("Способ доставки: ").append(deliveryMethod.getName()).append(System.lineSeparator());
            sb.append("Стоимость доставки: ").append(deliveryCost).append(System.lineSeparator());
            sb.append("Срок: ").append(deliveryMethod.estimateDeliveryTime()).append(System.lineSeparator());

            String details = deliveryMethod.getDetails();
            if (!details.isBlank()) {
                sb.append(details).append(System.lineSeparator());
            }
        }

        sb.append("Курьер: ")
                .append(courier == null ? "не назначен" : courier.getName())
                .append(System.lineSeparator());
        sb.append("Итого: ").append(getTotalCost()).append(System.lineSeparator());

        return sb.toString();
    }
}