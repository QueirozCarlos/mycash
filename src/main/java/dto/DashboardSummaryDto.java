package dto;

import model.MovementEntity;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryDto(
        BigDecimal balance,
        BigDecimal monthIncome,
        BigDecimal monthExpense,
        BigDecimal monthSavings,
        List<MovementEntity> recentMovements
) {
}
