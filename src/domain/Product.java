package domain;

import domain.DeliveryException;
import util.InputValidator;

import java.math.BigDecimal;

public class Product {
    private final int id;
    private final String name;
    private final BigDecimal price;
    private int availableQuantity;

    public Product(int id, String name, BigDecimal price, int availableQuantity) {
        if (id <= 0) {
            throw new DeliveryException("ID товара должен быть положительным.");
        }
        if (availableQuantity < 0) {
            throw new DeliveryException("Количество товара не может быть отрицательным.");
        }
        this.id = id;
        this.name = InputValidator.requireNonBlank(name, "Название товара");
        this.price = InputValidator.requirePositive(price, "Цена товара");
        this.availableQuantity = availableQuantity;
    }

    public void reserve(int quantity) {
        InputValidator.requirePositive(quantity, "Количество");
        if (quantity > availableQuantity) {
            throw new DeliveryException(
                    "Недостаточно товара \"" + name + "\" на складе. Доступно: " + availableQuantity + ".");
        }
        availableQuantity -= quantity;
    }

    public void release(int quantity) {
        InputValidator.requirePositive(quantity, "Количество");
        availableQuantity += quantity;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getAvailableQuantity() { return availableQuantity; }

    @Override
    public String toString() {
        return "ID " + id + ": " + name + " - " + price + " Руб (доступно: " + availableQuantity + ")";
    }
}