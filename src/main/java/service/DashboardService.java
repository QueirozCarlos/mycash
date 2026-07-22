package service;

import dto.CategoryBreakdownDto;
import dto.DashboardSummaryDto;
import dto.MonthlyComparisonDto;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;
import repository.JpaMovementRepository;
import repository.MovementRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardService {

    private static final DateTimeFormatter YEAR_MONTH_LABEL =
            DateTimeFormatter.ofPattern("MMM/yyyy", Locale.of("pt", "BR"));

    private final MovementRepository movementRepository;

    public DashboardService() {
        this(new JpaMovementRepository());
    }

    DashboardService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    public DashboardSummaryDto buildSummary() {
        BigDecimal income = movementRepository.sumByType(MovementType.RECEITA, MovementStatus.PAGO);
        BigDecimal expense = movementRepository.sumByType(MovementType.DESPESA, MovementStatus.PAGO);
        BigDecimal balance = income.subtract(expense);

        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();

        BigDecimal monthIncome = movementRepository.sumByTypeAndPeriodAndStatus(
                MovementType.RECEITA, start, end, MovementStatus.PAGO);
        BigDecimal monthExpense = movementRepository.sumByTypeAndPeriodAndStatus(
                MovementType.DESPESA, start, end, MovementStatus.PAGO);
        BigDecimal monthSavings = monthIncome.subtract(monthExpense);

        List<MovementEntity> recent = movementRepository.findRecent(8);
        return new DashboardSummaryDto(balance, monthIncome, monthExpense, monthSavings, recent);
    }

    public List<CategoryBreakdownDto> categoryBreakdown(YearMonth yearMonth) {
        if (yearMonth == null) {
            yearMonth = YearMonth.now();
        }

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<MovementEntity> movements = movementRepository.findByTypeAndPeriod(MovementType.DESPESA, start, end);

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, String> colors = new LinkedHashMap<>();
        for (MovementEntity movement : movements) {
            String name = movement.getCategoryName();
            totals.merge(name, movement.getAmount(), BigDecimal::add);
            colors.putIfAbsent(name, resolveCategoryColor(movement));
        }

        return totals.entrySet().stream()
                .map(entry -> new CategoryBreakdownDto(
                        entry.getKey(),
                        entry.getValue(),
                        colors.getOrDefault(entry.getKey(), "#5B6CFF")))
                .sorted(Comparator.comparing(CategoryBreakdownDto::amount).reversed())
                .toList();
    }

    private static String resolveCategoryColor(MovementEntity movement) {
        if (movement.getCategory() != null && movement.getCategory().getColorHex() != null
                && !movement.getCategory().getColorHex().isBlank()) {
            return movement.getCategory().getColorHex();
        }
        return "#5B6CFF";
    }

    public List<MonthlyComparisonDto> monthlyComparison(int lastMonths) {
        int months = Math.max(1, lastMonths);
        YearMonth current = YearMonth.now();
        List<MonthlyComparisonDto> comparisons = new ArrayList<>();

        for (int offset = months - 1; offset >= 0; offset--) {
            YearMonth month = current.minusMonths(offset);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();

            BigDecimal income = movementRepository.sumByTypeAndPeriodAndStatus(
                    MovementType.RECEITA, start, end, null);
            BigDecimal expense = movementRepository.sumByTypeAndPeriodAndStatus(
                    MovementType.DESPESA, start, end, null);

            comparisons.add(new MonthlyComparisonDto(
                    YEAR_MONTH_LABEL.format(month),
                    income,
                    expense));
        }

        return comparisons;
    }

    public List<MonthlyComparisonDto> monthlyComparison() {
        return monthlyComparison(6);
    }
}
