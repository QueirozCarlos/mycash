package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.GoalEntity;
import service.GoalService;
import util.Formatters;
import util.UiDialogs;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GoalsController {

    @FXML
    private TableView<GoalEntity> table;

    @FXML
    private TableColumn<GoalEntity, String> nameColumn;

    @FXML
    private TableColumn<GoalEntity, BigDecimal> targetColumn;

    @FXML
    private TableColumn<GoalEntity, BigDecimal> currentColumn;

    @FXML
    private TableColumn<GoalEntity, Number> progressColumn;

    @FXML
    private TableColumn<GoalEntity, LocalDate> deadlineColumn;

    @FXML
    private TableColumn<GoalEntity, Boolean> activeColumn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private TextField progressAmountField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField targetField;

    @FXML
    private TextField currentField;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private CheckBox activeCheck;

    @FXML
    private Button saveButton;

    private final GoalService goalService = new GoalService();
    private final ObservableList<GoalEntity> items = FXCollections.observableArrayList();
    private GoalEntity selected;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("targetAmount"));
        currentColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progressPercent"));
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        targetColumn.setCellFactory(column -> currencyCell());
        currentColumn.setCellFactory(column -> currencyCell());
        deadlineColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatDate(item));
            }
        });
        progressColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.1f%%", item.doubleValue()));
            }
        });

        table.setItems(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selected = newValue;
            fillForm(newValue);
            updateProgressUi(newValue);
        });

        clearForm();
        refresh();
    }

    @FXML
    private void onSave() {
        try {
            BigDecimal target = Formatters.parseCurrency(targetField.getText());
            BigDecimal current = currentField.getText() == null || currentField.getText().isBlank()
                    ? BigDecimal.ZERO
                    : Formatters.parseCurrency(currentField.getText());
            String description = blankToNull(descriptionArea.getText());

            GoalEntity persisted;
            if (selected == null) {
                persisted = goalService.create(
                        nameField.getText(),
                        description,
                        target,
                        current,
                        deadlinePicker.getValue());
            } else {
                persisted = goalService.update(
                        selected.getId(),
                        nameField.getText(),
                        description,
                        target,
                        current,
                        deadlinePicker.getValue(),
                        activeCheck.isSelected());
            }
            selected = persisted;
            refresh();
            table.getSelectionModel().select(persisted);
            updateProgressUi(persisted);
            UiDialogs.info("Meta salva com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao salvar meta", exception);
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            UiDialogs.error("Selecione uma meta para excluir.");
            return;
        }
        if (!UiDialogs.confirm("Excluir a meta \"" + selected.getName() + "\"?")) {
            return;
        }
        try {
            goalService.delete(selected.getId());
            clearForm();
            refresh();
            updateProgressUi(null);
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao excluir meta", exception);
        }
    }

    @FXML
    private void onClear() {
        clearForm();
        table.getSelectionModel().clearSelection();
        updateProgressUi(null);
    }

    @FXML
    private void onAddProgress() {
        if (selected == null) {
            UiDialogs.error("Selecione uma meta para adicionar progresso.");
            return;
        }
        try {
            BigDecimal amount = Formatters.parseCurrency(progressAmountField.getText());
            GoalEntity updated = goalService.addProgress(selected.getId(), amount);
            selected = updated;
            progressAmountField.clear();
            refresh();
            table.getSelectionModel().select(updated);
            fillForm(updated);
            updateProgressUi(updated);
            UiDialogs.info("Progresso adicionado com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao adicionar progresso", exception);
        }
    }

    private void refresh() {
        try {
            items.setAll(goalService.list());
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao listar metas", exception);
        }
    }

    private void fillForm(GoalEntity goal) {
        if (goal == null) {
            clearForm();
            return;
        }
        nameField.setText(goal.getName());
        descriptionArea.setText(goal.getDescription());
        targetField.setText(goal.getTargetAmount().toPlainString().replace('.', ','));
        currentField.setText(goal.getCurrentAmount().toPlainString().replace('.', ','));
        deadlinePicker.setValue(goal.getDeadline());
        activeCheck.setSelected(goal.isActive());
        saveButton.setText("Atualizar");
    }

    private void clearForm() {
        selected = null;
        nameField.clear();
        descriptionArea.clear();
        targetField.clear();
        currentField.setText("0,00");
        deadlinePicker.setValue(null);
        activeCheck.setSelected(true);
        saveButton.setText("Salvar");
    }

    private void updateProgressUi(GoalEntity goal) {
        if (goal == null) {
            progressBar.setProgress(0);
            progressLabel.setText("0%");
            return;
        }
        double percent = Math.min(100.0, Math.max(0.0, goal.getProgressPercent()));
        progressBar.setProgress(percent / 100.0);
        progressLabel.setText(String.format(
                "%.1f%% — %s de %s",
                percent,
                Formatters.formatCurrency(goal.getCurrentAmount()),
                Formatters.formatCurrency(goal.getTargetAmount())));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static TableCell<GoalEntity, BigDecimal> currencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatCurrency(item));
            }
        };
    }
}
