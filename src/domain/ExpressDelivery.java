package domain;

import java.math.BigDecimal;

public final class ExpressDelivery implements DeliveryMethod {
    private static final BigDecimal COST = BigDecimal.valueOf(700);

    @Override
    public String getName() {
        return "Экспресс-доставка";
    }

    @Override
    public BigDecimal calculateCost(BigDecimal itemsTotal) {
        return COST;
    }

    @Override
    public String estimateDeliveryTime() {
        return "1 день";
    }

    @Override
    public boolean requiresCourier() {
        return true;
    }
}