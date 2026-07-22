package dto;

import java.math.BigDecimal;

public record MonthlyComparisonDto(
        String yearMonth,
        BigDecimal income,
        BigDecimal expense
) {
}
