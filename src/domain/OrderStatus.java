package domain;

public enum OrderStatus {
    NEW("Новый"),
    CONFIRMED("Подтверждён"),
    ASSIGNED("Курьер назначен"),
    READY_FOR_PICKUP("Готов к самовывозу"),
    IN_DELIVERY("В доставке"),
    COMPLETED("Завершён"),
    CANCELLED("Отменён");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case NEW -> target == CONFIRMED || target == CANCELLED;
            case CONFIRMED -> target == ASSIGNED || target == READY_FOR_PICKUP || target == CANCELLED;
            case ASSIGNED -> target == IN_DELIVERY || target == CANCELLED;
            case READY_FOR_PICKUP -> target == COMPLETED || target == CANCELLED;
            case IN_DELIVERY -> target == COMPLETED || target == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}