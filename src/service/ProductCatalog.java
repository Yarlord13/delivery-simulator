package service;

import domain.DeliveryException;
import domain.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductCatalog {
    private final List<Product> products = new ArrayList<>();

    public ProductCatalog() {
        products.add(new Product(1, "Книга",       new BigDecimal("750.00"),   10));
        products.add(new Product(2, "Наушники",    new BigDecimal("2990.00"),   5));
        products.add(new Product(3, "Клавиатура",  new BigDecimal("4500.00"),   3));
        products.add(new Product(4, "Мышь",        new BigDecimal("1200.00"),   8));
        products.add(new Product(5, "Монитор",     new BigDecimal("18990.00"),  2));
    }

    public List<Product> getAll() {
        return List.copyOf(products);
    }

    public List<Product> getAvailable() {
        return products.stream()
                .filter(p -> p.getAvailableQuantity() > 0)
                .toList();
    }

    public Product getById(int id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new DeliveryException("Товар с ID " + id + " не найден."));
    }
}