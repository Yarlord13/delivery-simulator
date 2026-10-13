package domain;

import java.math.BigDecimal;

public final class StandardDelivery implements DeliveryMethod {
    private static final BigDecimal BASE_COST = BigDecimal.valueOf(300);
    private static final BigDecimal FREE_FROM = BigDecimal.valueOf(5000);

    @Override
    public String getName() {
        return "Стандартная доставка";
    }

    @Override
    public BigDecimal calculateCost(BigDecimal itemsTotal) {
        if (itemsTotal == null) {
            throw new DeliveryException("Сумма позиций не может быть null.");
        }
        return itemsTotal.compareTo(FREE_FROM) >= 0 ? BigDecimal.ZERO : BASE_COST;
    }

    @Override
    public String estimateDeliveryTime() {
        return "2-4 дня";
    }

    @Override
    public boolean requiresCourier() {
        return true;
    }
}