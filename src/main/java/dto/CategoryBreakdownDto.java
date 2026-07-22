package dto;

import java.math.BigDecimal;

public record CategoryBreakdownDto(
        String categoryName,
        BigDecimal amount,
        String colorHex
) {
}
