package domain;

import java.math.BigDecimal;

public interface DeliveryMethod {
    String getName();

    BigDecimal calculateCost(BigDecimal itemsTotal);

    String estimateDeliveryTime();

    boolean requiresCourier();

    default String getDetails() {
        return "";
    }
}