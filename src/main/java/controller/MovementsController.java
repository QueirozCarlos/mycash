package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.CategoryEntity;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;
import service.CategoryService;
import service.MovementService;
import util.Formatters;
import util.UiDialogs;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MovementsController {

    @FXML
    private Label sectionTitle;

    @FXML
    private TextField searchField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<MovementEntity> table;

    @FXML
    private TableColumn<MovementEntity, Long> idColumn;

    @FXML
    private TableColumn<MovementEntity, String> descriptionColumn;

    @FXML
    private TableColumn<MovementEntity, String> categoryColumn;

    @FXML
    private TableColumn<MovementEntity, BigDecimal> amountColumn;

    @FXML
    private TableColumn<MovementEntity, LocalDate> dateColumn;

    @FXML
    private TableColumn<MovementEntity, LocalDate> dueDateColumn;

    @FXML
    private TableColumn<MovementEntity, MovementStatus> statusColumn;

    @FXML
    private TextField descriptionField;

    @FXML
    private ComboBox<CategoryEntity> categoryCombo;

    @FXML
    private TextField amountField;

    @FXML
    private DatePicker movementDatePicker;

    @FXML
    private VBox dueDateBox;

    @FXML
    private DatePicker dueDatePicker;

    @FXML
    private ComboBox<MovementStatus> statusCombo;

    @FXML
    private TextArea notesArea;

    @FXML
    private CheckBox recurringCheck;

    @FXML
    private Button saveButton;

    private final MovementService movementService = new MovementService();
    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<MovementEntity> items = FXCollections.observableArrayList();

    private MovementType type = MovementType.RECEITA;
    private MovementEntity selected;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("movementDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        amountColumn.setCellFactory(column -> currencyCell());
        dateColumn.setCellFactory(column -> dateCell());
        dueDateColumn.setCellFactory(column -> dateCell());

        statusCombo.setItems(FXCollections.observableArrayList(MovementStatus.values()));
        categoryCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoryEntity category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public CategoryEntity fromString(String string) {
                return null;
            }
        });

        table.setItems(items);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selected = newValue;
            fillForm(newValue);
        });

        movementDatePicker.setValue(LocalDate.now());
        clearFormDefaults();
        loadCategories();
        applyTypeUi();
    }

    public void setType(MovementType type) {
        this.type = type == null ? MovementType.RECEITA : type;
        applyTypeUi();
        refresh();
    }

    @FXML
    private void onFilter() {
        refresh();
    }

    @FXML
    private void onSave() {
        try {
            BigDecimal amount = Formatters.parseCurrency(amountField.getText());
            LocalDate movementDate = movementDatePicker.getValue();
            LocalDate dueDate = type == MovementType.DESPESA ? dueDatePicker.getValue() : null;
            CategoryEntity category = categoryCombo.getValue();
            MovementStatus status = statusCombo.getValue();
            String notes = blankToNull(notesArea.getText());
            boolean recurring = type == MovementType.DESPESA && recurringCheck.isSelected();

            MovementEntity persisted;
            if (selected == null) {
                if (type == MovementType.RECEITA) {
                    persisted = movementService.createIncome(
                            descriptionField.getText(), amount, movementDate, category, notes, status);
                } else {
                    persisted = movementService.createExpense(
                            descriptionField.getText(), amount, movementDate, dueDate, category, notes,
                            status, recurring);
                }
            } else {
                persisted = movementService.update(
                        selected.getId(),
                        descriptionField.getText(),
                        amount,
                        movementDate,
                        dueDate,
                        category,
                        notes,
                        status,
                        recurring);
            }

            selected = persisted;
            refresh();
            table.getSelectionModel().select(persisted);
            UiDialogs.info("Movimentação salva com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao salvar movimentação", exception);
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            UiDialogs.error("Selecione uma movimentação para excluir.");
            return;
        }
        if (!UiDialogs.confirm("Excluir a movimentação \"" + selected.getDescription() + "\"?")) {
            return;
        }
        try {
            movementService.delete(selected.getId());
            clearForm();
            refresh();
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao excluir movimentação", exception);
        }
    }

    @FXML
    private void onClear() {
        clearForm();
        table.getSelectionModel().clearSelection();
    }

    private void refresh() {
        try {
            items.setAll(movementService.search(
                    type,
                    searchField.getText(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue()));
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao listar movimentações", exception);
        }
    }

    private void loadCategories() {
        try {
            categoryCombo.setItems(FXCollections.observableArrayList(categoryService.list(null)));
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao carregar categorias", exception);
        }
    }

    private void applyTypeUi() {
        boolean expense = type == MovementType.DESPESA;
        sectionTitle.setText(expense ? "Despesas" : "Receitas");
        dueDateBox.setVisible(expense);
        dueDateBox.setManaged(expense);
        recurringCheck.setVisible(expense);
        recurringCheck.setManaged(expense);
        if (!expense) {
            recurringCheck.setSelected(false);
            dueDatePicker.setValue(null);
        }
        if (statusCombo.getValue() == null) {
            statusCombo.setValue(expense ? MovementStatus.PENDENTE : MovementStatus.PAGO);
        }
    }

    private void fillForm(MovementEntity movement) {
        if (movement == null) {
            clearForm();
            return;
        }
        descriptionField.setText(movement.getDescription());
        categoryCombo.setValue(movement.getCategory());
        amountField.setText(movement.getAmount() == null ? "" : movement.getAmount().toPlainString().replace('.', ','));
        movementDatePicker.setValue(movement.getMovementDate());
        dueDatePicker.setValue(movement.getDueDate());
        statusCombo.setValue(movement.getStatus());
        notesArea.setText(movement.getNotes());
        recurringCheck.setSelected(movement.isRecurring());
        saveButton.setText("Atualizar");
    }

    private void clearForm() {
        selected = null;
        clearFormDefaults();
        saveButton.setText("Salvar");
    }

    private void clearFormDefaults() {
        descriptionField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        amountField.clear();
        movementDatePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(null);
        statusCombo.setValue(type == MovementType.DESPESA ? MovementStatus.PENDENTE : MovementStatus.PAGO);
        notesArea.clear();
        recurringCheck.setSelected(false);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static TableCell<MovementEntity, BigDecimal> currencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatCurrency(item));
            }
        };
    }

    private static TableCell<MovementEntity, LocalDate> dateCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatDate(item));
            }
        };
    }
}
