package domain;

import domain.DeliveryException;
import domain.Product;
import util.InputValidator;

import java.math.BigDecimal;

public final class OrderItem {
    private final Product product;
    private final int quantity;

    public OrderItem(Product product, int quantity) {
        if (product == null) {
            throw new DeliveryException("Товар не может быть null.");
        }
        this.product = product;
        this.quantity = InputValidator.requirePositive(quantity, "Количество");
    }

    public Product getProduct() { return product; }
    public int getQuantity()    { return quantity; }

    public BigDecimal getTotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return product.getName() + " x" + quantity + " = " + getTotal();
    }
}