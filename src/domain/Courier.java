package domain;

public class Courier {
    private final int id;
    private final String name;
    private final String phone;
    private boolean available;

    public Courier(int id, String name, String phone) {
        if (id <= 0) {
            throw new DeliveryException("ID курьера должен быть положительным.");
        }
        this.id = id;
        this.name = requireNonBlank(name, "Имя курьера");
        this.phone = requireNonBlank(phone, "Телефон курьера");
        this.available = true;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DeliveryException(fieldName + " не может быть пустым.");
        }
        return value.trim();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAvailable() {
        return available;
    }

    public void markBusy() {
        available = false;
    }

    public void markAvailable() {
        available = true;
    }

    @Override
    public String toString() {
        return "ID " + id + ": " + name + ", тел. " + phone;
    }
}