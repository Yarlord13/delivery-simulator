package ui;

import domain.Courier;
import domain.Customer;
import domain.DeliveryException;
import domain.DeliveryMethod;
import domain.ExpressDelivery;
import domain.Order;
import domain.OrderItem;
import domain.OrderStatus;
import domain.PickupDelivery;
import domain.StandardDelivery;
import service.OrderService;

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

        String name = readNonEmpty("Имя клиента: ");
        String phone = readNonEmpty("Телефон клиента: ");
        String address = readNonEmpty("Адрес доставки: ");

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
        String itemName = readNonEmpty("Название товара: ");
        int quantity = readPositiveInt("Количество: ");
        BigDecimal price = readPositiveBigDecimal("Цена за единицу: ");

        orderService.addItem(orderNumber, new OrderItem(itemName, quantity, price));
        System.out.println("Позиция добавлена.");
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

        List<Courier> available = orderService.getAvailableCouriers();

        if (available.isEmpty()) {
            System.out.println("Нет доступных курьеров.");
            return;
        }

        System.out.println("Доступные курьеры:");
        for (Courier courier : available) {
            System.out.println(courier);
        }

        int courierId = readInt("ID курьера: ");
        Courier courier = orderService.getCourier(courierId);

        orderService.assignCourier(orderNumber, courier);
        System.out.println("Курьер " + courier.getName() + " назначен на заказ №" + orderNumber + ".");
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
            if (value > 0) {
                return value;
            }
            System.out.println("Число должно быть положительным.");
        }
    }

    private BigDecimal readPositiveBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().replace(',', '.');

            try {
                BigDecimal value = new BigDecimal(line);
                if (value.compareTo(BigDecimal.ZERO) > 0) {
                    return value;
                }
                System.out.println("Число должно быть положительным.");
            } catch (NumberFormatException e) {
                System.out.println("Введите число, например 199.99.");
            }
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();

            if (!line.isEmpty()) {
                return line;
            }

            System.out.println("Поле не может быть пустым.");
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String answer = readNonEmpty(prompt).toLowerCase();

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