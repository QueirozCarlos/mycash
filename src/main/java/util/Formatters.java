package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formatters {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(PT_BR);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formatters() {
    }

    public static String formatCurrency(Number value) {
        if (value == null) {
            return CURRENCY_FORMATTER.format(0);
        }
        return CURRENCY_FORMATTER.format(value);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMATTER.format(date);
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value.trim(), DATE_FORMATTER);
    }

    /**
     * Accepts values like {@code 1.234,56}, {@code 1234,56}, {@code R$ 1.234,56} or {@code 1234.56}.
     */
    public static BigDecimal parseCurrency(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Valor monetário não pode estar vazio.");
        }

        String normalized = value.trim()
                .replace("R$", "")
                .replace("\u00A0", "")
                .replace(" ", "")
                .trim();

        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(",", ".");
        }

        try {
            return new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Valor monetário inválido: " + value, exception);
        }
    }
}
