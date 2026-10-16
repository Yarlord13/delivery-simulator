package domain;

import util.InputValidator;

public final class Customer {
    private final String name;
    private final String phone;

    public Customer(String name, String phone) {
        this.name = InputValidator.requireNonBlank(name, "Имя клиента");
        this.phone = InputValidator.requirePhone(phone, "Телефон клиента");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DeliveryException(fieldName + " не может быть пустым.");
        }
        return value.trim();
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return name + " (" + phone + ")";
    }
}