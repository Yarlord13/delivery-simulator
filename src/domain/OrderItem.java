package domain;

import java.math.BigDecimal;

public final class OrderItem {
    private final String name;
    private final int quantity;
    private final BigDecimal price;

    public OrderItem(String name, int quantity, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new DeliveryException("Название товара не может быть пустым.");
        }
        if (quantity <= 0) {
            throw new DeliveryException("Количество должно быть положительным.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DeliveryException("Цена должна быть положительной.");
        }

        this.name = name.trim();
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return name + " x" + quantity + " = " + getTotal();
    }
}