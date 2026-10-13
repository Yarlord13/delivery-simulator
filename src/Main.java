import service.OrderService;
import ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        new ConsoleUI(new OrderService()).start();
    }
}