package domain;

import java.math.BigDecimal;

public final class PickupDelivery implements DeliveryMethod {
    private final String pickupAddress;

    public PickupDelivery(String pickupAddress) {
        if (pickupAddress == null || pickupAddress.isBlank()) {
            throw new DeliveryException("Адрес пункта самовывоза не может быть пустым.");
        }
        this.pickupAddress = pickupAddress.trim();
    }

    @Override
    public String getName() {
        return "Самовывоз";
    }

    @Override
    public BigDecimal calculateCost(BigDecimal itemsTotal) {
        return BigDecimal.ZERO;
    }

    @Override
    public String estimateDeliveryTime() {
        return "сегодня после подтверждения";
    }

    @Override
    public boolean requiresCourier() {
        return false;
    }

    @Override
    public String getDetails() {
        return "Пункт самовывоза: " + pickupAddress;
    }
}