package util;

import domain.DeliveryException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class InputValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d{7,15}$");

    private InputValidator() {
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DeliveryException(fieldName + " не может быть пустым.");
        }
        return value.trim();
    }

    public static String requirePhone(String value, String fieldName) {
        String trimmed = requireNonBlank(value, fieldName);
        String normalized = trimmed.replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new DeliveryException(
                    fieldName + " должен содержать 7–15 цифр; допускается ведущий '+' " + "(например +79991234567).");
        }
        return trimmed;
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new DeliveryException(fieldName + " должно быть положительным.");
        }
        return value;
    }

    public static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DeliveryException(fieldName + " должно быть положительным.");
        }
        return value;
    }
}