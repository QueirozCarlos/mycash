package controller;

import dto.CategoryBreakdownDto;
import dto.DashboardSummaryDto;
import dto.MonthlyComparisonDto;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;
import service.DashboardService;
import util.ColorUtils;
import util.Formatters;
import util.UiDialogs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class DashboardController {

    @FXML
    private Label balanceLabel;

    @FXML
    private Label monthIncomeLabel;

    @FXML
    private Label monthExpenseLabel;

    @FXML
    private Label monthSavingsLabel;

    @FXML
    private TableView<MovementEntity> recentTable;

    @FXML
    private TableColumn<MovementEntity, String> descriptionColumn;

    @FXML
    private TableColumn<MovementEntity, MovementType> typeColumn;

    @FXML
    private TableColumn<MovementEntity, BigDecimal> amountColumn;

    @FXML
    private TableColumn<MovementEntity, LocalDate> dateColumn;

    @FXML
    private TableColumn<MovementEntity, MovementStatus> statusColumn;

    @FXML
    private BarChart<String, Number> monthlyChart;

    @FXML
    private PieChart categoryChart;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    private void initialize() {
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("movementDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatCurrency(item));
            }
        });
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatDate(item));
            }
        });

        refresh();
    }

    private void refresh() {
        try {
            DashboardSummaryDto summary = dashboardService.buildSummary();
            balanceLabel.setText(Formatters.formatCurrency(summary.balance()));
            monthIncomeLabel.setText(Formatters.formatCurrency(summary.monthIncome()));
            monthExpenseLabel.setText(Formatters.formatCurrency(summary.monthExpense()));
            monthSavingsLabel.setText(Formatters.formatCurrency(summary.monthSavings()));
            recentTable.setItems(FXCollections.observableArrayList(summary.recentMovements()));

            fillMonthlyChart(dashboardService.monthlyComparison());
            fillCategoryChart(dashboardService.categoryBreakdown(YearMonth.now()));
        } catch (RuntimeException exception) {
            UiDialogs.error("Não foi possível carregar o dashboard", exception);
        }
    }

    private void fillMonthlyChart(List<MonthlyComparisonDto> comparisons) {
        monthlyChart.getData().clear();
        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Receitas");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Despesas");

        for (MonthlyComparisonDto comparison : comparisons) {
            incomeSeries.getData().add(new XYChart.Data<>(comparison.yearMonth(), comparison.income()));
            expenseSeries.getData().add(new XYChart.Data<>(comparison.yearMonth(), comparison.expense()));
        }

        monthlyChart.getData().addAll(incomeSeries, expenseSeries);
    }

    private void fillCategoryChart(List<CategoryBreakdownDto> breakdown) {
        categoryChart.getData().clear();
        if (breakdown.isEmpty()) {
            PieChart.Data empty = new PieChart.Data("Sem despesas", 1);
            categoryChart.setData(FXCollections.observableArrayList(empty));
            applyPieColor(empty, "#94A3B8");
            return;
        }

        List<PieChart.Data> data = breakdown.stream()
                .map(item -> new PieChart.Data(
                        item.categoryName() + " (" + Formatters.formatCurrency(item.amount()) + ")",
                        item.amount().doubleValue()))
                .toList();
        categoryChart.setData(FXCollections.observableArrayList(data));

        for (int index = 0; index < data.size(); index++) {
            applyPieColor(data.get(index), breakdown.get(index).colorHex());
        }
    }

    private void applyPieColor(PieChart.Data slice, String colorHex) {
        String cssColor = ColorUtils.toHex(ColorUtils.fromHex(colorHex));
        Runnable apply = () -> {
            if (slice.getNode() != null) {
                slice.getNode().setStyle("-fx-pie-color: " + cssColor + ";");
            }
        };
        if (slice.getNode() != null) {
            apply.run();
        } else {
            slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    apply.run();
                }
            });
        }
    }
}
