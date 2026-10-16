package ui;

import domain.*;
import service.OrderService;
import util.InputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);
    private final OrderService orderService;

    public ConsoleUI(OrderService orderService) {
        this.orderService = orderService;
    }

    public void start() {
        System.out.println("=== Консольный симулятор службы доставки ===");

        while (true) {
            printMenu();
            int choice = readInt("Выберите пункт: ");

            try {
                switch (choice) {
                    case 1 -> createOrder();
                    case 2 -> showOrders();
                    case 3 -> addItemToOrder();
                    case 4 -> selectDeliveryMethod();
                    case 5 -> assignCourier();
                    case 6 -> changeStatus();
                    case 7 -> showOrderDetails();
                    case 0 -> {
                        System.out.println("Работа программы завершена.");
                        return;
                    }
                    default -> System.out.println("Некорректный пункт меню.");
                }
            } catch (DeliveryException | IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Создать заказ");
        System.out.println("2. Показать список заказов");
        System.out.println("3. Добавить позицию в заказ");
        System.out.println("4. Выбрать способ доставки");
        System.out.println("5. Назначить курьера");
        System.out.println("6. Изменить статус заказа");
        System.out.println("7. Показать информацию о заказе");
        System.out.println("0. Выход");
    }

    private void createOrder() {
        System.out.println("Создание заказа");

        String name = readNonEmpty("Имя клиента: ", "Имя клиента");
        String phone = readPhone("Телефон клиента: ");
        String address = readNonEmpty("Адрес доставки: ", "Адрес доставки");

        Customer customer = new Customer(name, phone);
        Order order = orderService.createOrder(customer, address);

        System.out.println("Создан заказ №" + order.getNumber());

        if (readYesNo("Добавить позиции сейчас? (да/нет): ")) {
            do {
                addItemToOrder(order.getNumber());
            } while (readYesNo("Добавить ещё позицию? (да/нет): "));
        }
    }

    private void showOrders() {
        List<Order> orders = orderService.getOrders();

        if (orders.isEmpty()) {
            System.out.println("Заказов пока нет.");
            return;
        }

        for (Order order : orders) {
            System.out.println(order.toShortString());
        }
    }

    private void addItemToOrder() {
        if (orderService.getOrders().isEmpty()) {
            System.out.println("Заказов пока нет.");
            return;
        }

        int orderNumber = readInt("Номер заказа: ");
        addItemToOrder(orderNumber);
    }

    private void addItemToOrder(int orderNumber) {
        Order order = orderService.getOrder(orderNumber);
        order.checkCanAddItems();              // бизнес-проверка до диалога

        List<Product> available = orderService.getCatalog().getAvailable();
        if (available.isEmpty()) {
            System.out.println("Нет доступных товаров.");
            return;
        }

        System.out.println("Доступные товары:");
        for (Product p : available) {
            System.out.println(p);
        }

        Product product = readAvailableProduct(available);
        int quantity = readQuantityWithinStock(product);

        orderService.addItem(orderNumber, product.getId(), quantity);
        System.out.println("Позиция добавлена.");
    }

    private Product readAvailableProduct(List<Product> available) {
        while (true) {
            int id = readPositiveInt("ID товара: ");
            Product product = available.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (product != null) {
                return product;
            }
            System.out.println("Товар с ID " + id + " отсутствует в списке доступных.");
        }
    }

    private int readQuantityWithinStock(Product product) {
        while (true) {
            int q = readPositiveInt("Количество: ");
            if (q <= product.getAvailableQuantity()) {
                return q;
            }
            System.out.println("Недостаточно товара. Доступно: " + product.getAvailableQuantity());
        }
    }

    private void selectDeliveryMethod() {
        if (orderService.getOrders().isEmpty()) {
            System.out.println("Заказов пока нет.");
            return;
        }

        int orderNumber = readInt("Номер заказа: ");
        DeliveryMethod method = chooseDeliveryMethod();

        orderService.selectDeliveryMethod(orderNumber, method);

        Order order = orderService.getOrder(orderNumber);

        System.out.println("Способ доставки выбран: " + method.getName());
        System.out.println("Стоимость доставки: " + order.getDeliveryCost());
        System.out.println("Итого: " + order.getTotalCost());
        System.out.println("Ориентировочный срок: " + method.estimateDeliveryTime());
    }

    private DeliveryMethod chooseDeliveryMethod() {
        System.out.println("1. Стандартная доставка");
        System.out.println("2. Экспресс-доставка");
        System.out.println("3. Самовывоз");

        int choice = readInt("Выберите способ: ");

        return switch (choice) {
            case 1 -> new StandardDelivery();
            case 2 -> new ExpressDelivery();
            case 3 -> new PickupDelivery("ул. Ленина, 10");
            default -> throw new DeliveryException("Некорректный способ доставки.");
        };
    }

    private void assignCourier() {
        if (orderService.getOrders().isEmpty()) {
            System.out.println("Заказов пока нет.");
            return;
        }

        int orderNumber = readInt("Номер заказа: ");
        Order order = orderService.getOrder(orderNumber);
        order.checkCanAssignCourier();         // до запроса данных

        List<Courier> available = orderService.getAvailableCouriers();
        if (available.isEmpty()) {
            System.out.println("Нет доступных курьеров.");
            return;
        }

        System.out.println("Доступные курьеры:");
        for (Courier c : available) {
            System.out.println(c);
        }

        Courier courier = readAvailableCourier(available);

        orderService.assignCourier(orderNumber, courier);
        System.out.println("Курьер " + courier.getName() + " назначен на заказ №" + orderNumber + ".");
    }

    private Courier readAvailableCourier(List<Courier> available) {
        while (true) {
            int id = readPositiveInt("ID курьера: ");
            Courier courier = available.stream()
                    .filter(c -> c.getId() == id)
                    .findFirst()
                    .orElse(null);
            if (courier != null) {
                return courier;
            }
            System.out.println("Курьер с ID " + id + " отсутствует в списке доступных.");
        }
    }

    private void changeStatus() {
        if (orderService.getOrders().isEmpty()) {
            System.out.println("Заказов пока нет.");
            return;
        }

        int orderNumber = readInt("Номер заказа: ");
        Order order = orderService.getOrder(orderNumber);

        System.out.println("Текущий статус: " + order.getStatus().getTitle());

        OrderStatus[] statuses = OrderStatus.values();

        System.out.println("Выберите новый статус:");
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i].getTitle());
        }

        int statusChoice = readInt("Номер статуса: ");
        if (statusChoice < 1 || statusChoice > statuses.length) {
            throw new DeliveryException("Некорректный номер статуса.");
        }

        OrderStatus newStatus = statuses[statusChoice - 1];
        orderService.changeStatus(orderNumber, newStatus);

        System.out.println("Статус заказа №" + orderNumber + " изменён на \"" + newStatus.getTitle() + "\".");
    }

    private void showOrderDetails() {
        if (orderService.getOrders().isEmpty()) {
            System.out.println("Заказов пока нет.");
            return;
        }

        int orderNumber = readInt("Номер заказа: ");
        System.out.println(orderService.getOrder(orderNumber).toDetailedString());
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            try {
                return InputValidator.requirePositive(value, "Число");
            } catch (DeliveryException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String readNonEmpty(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return InputValidator.requireNonBlank(line, fieldName);
            } catch (DeliveryException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String readPhone(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return InputValidator.requirePhone(line, "Телефон");
            } catch (DeliveryException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String answer = readNonEmpty(prompt, "Ответ").toLowerCase();
            if (answer.equals("да") || answer.equals("д") || answer.equals("yes") || answer.equals("y")) {
                return true;
            }
            if (answer.equals("нет") || answer.equals("н") || answer.equals("no") || answer.equals("n")) {
                return false;
            }
            System.out.println("Ответьте \"да\" или \"нет\".");
        }
    }
}