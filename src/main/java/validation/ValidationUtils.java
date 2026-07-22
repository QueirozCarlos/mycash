package validation;

import java.math.BigDecimal;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " não pode estar vazio.");
        }
    }

    public static void requirePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " deve ser um valor positivo.");
        }
    }

    public static void requireDayOfMonth(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Dia do mês deve estar entre 1 e 31.");
        }
    }
}
