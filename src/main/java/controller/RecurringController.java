package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.CategoryEntity;
import model.MovementType;
import model.RecurrenceFrequency;
import model.RecurringAccountEntity;
import service.CategoryService;
import service.RecurringAccountService;
import util.Formatters;
import util.UiDialogs;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringController {

    @FXML
    private TableView<RecurringAccountEntity> table;

    @FXML
    private TableColumn<RecurringAccountEntity, String> descriptionColumn;

    @FXML
    private TableColumn<RecurringAccountEntity, MovementType> typeColumn;

    @FXML
    private TableColumn<RecurringAccountEntity, BigDecimal> amountColumn;

    @FXML
    private TableColumn<RecurringAccountEntity, String> categoryColumn;

    @FXML
    private TableColumn<RecurringAccountEntity, RecurrenceFrequency> frequencyColumn;

    @FXML
    private TableColumn<RecurringAccountEntity, LocalDate> nextColumn;

    @FXML
    private TableColumn<RecurringAccountEntity, Boolean> activeColumn;

    @FXML
    private TextField descriptionField;

    @FXML
    private ComboBox<MovementType> typeCombo;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<CategoryEntity> categoryCombo;

    @FXML
    private ComboBox<RecurrenceFrequency> frequencyCombo;

    @FXML
    private TextField intervalField;

    @FXML
    private DatePicker nextOccurrencePicker;

    @FXML
    private TextArea notesArea;

    @FXML
    private CheckBox activeCheck;

    @FXML
    private Button saveButton;

    private final RecurringAccountService recurringAccountService = new RecurringAccountService();
    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<RecurringAccountEntity> items = FXCollections.observableArrayList();
    private RecurringAccountEntity selected;

    @FXML
    private void initialize() {
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        frequencyColumn.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        nextColumn.setCellValueFactory(new PropertyValueFactory<>("nextOccurrence"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatCurrency(item));
            }
        });
        nextColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatDate(item));
            }
        });

        typeCombo.setItems(FXCollections.observableArrayList(MovementType.values()));
        frequencyCombo.setItems(FXCollections.observableArrayList(RecurrenceFrequency.values()));
        categoryCombo.setConverter(categoryConverter());

        table.setItems(items);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selected = newValue;
            fillForm(newValue);
        });

        loadCategories();
        clearForm();
        refresh();
    }

    @FXML
    private void onSave() {
        try {
            BigDecimal amount = Formatters.parseCurrency(amountField.getText());
            Integer interval = parseInterval(intervalField.getText());
            String notes = blankToNull(notesArea.getText());

            RecurringAccountEntity persisted;
            if (selected == null) {
                persisted = recurringAccountService.create(
                        descriptionField.getText(),
                        typeCombo.getValue(),
                        amount,
                        categoryCombo.getValue(),
                        frequencyCombo.getValue(),
                        interval,
                        nextOccurrencePicker.getValue(),
                        notes);
            } else {
                persisted = recurringAccountService.update(
                        selected.getId(),
                        descriptionField.getText(),
                        typeCombo.getValue(),
                        amount,
                        categoryCombo.getValue(),
                        frequencyCombo.getValue(),
                        interval,
                        nextOccurrencePicker.getValue(),
                        activeCheck.isSelected(),
                        notes);
            }
            selected = persisted;
            refresh();
            table.getSelectionModel().select(persisted);

            String destino = persisted.getType() == MovementType.DESPESA ? "Despesas" : "Receitas";
            UiDialogs.info("Conta recorrente salva. Se a próxima ocorrência já venceu (ou é hoje), "
                    + "a movimentação aparece em " + destino + ".");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao salvar conta recorrente", exception);
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            UiDialogs.error("Selecione uma conta recorrente para excluir.");
            return;
        }
        if (!UiDialogs.confirm("Excluir a conta \"" + selected.getDescription() + "\"?")) {
            return;
        }
        try {
            recurringAccountService.delete(selected.getId());
            clearForm();
            refresh();
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao excluir conta recorrente", exception);
        }
    }

    @FXML
    private void onClear() {
        clearForm();
        table.getSelectionModel().clearSelection();
    }

    @FXML
    private void onGenerate() {
        try {
            int created = recurringAccountService.generateDueOccurrences();
            refresh();
            UiDialogs.info(created == 0
                    ? "Nenhuma ocorrência pendente para gerar."
                    : created + " movimentação(ões) gerada(s).");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao gerar ocorrências", exception);
        }
    }

    private void refresh() {
        try {
            items.setAll(recurringAccountService.list());
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao listar contas recorrentes", exception);
        }
    }

    private void loadCategories() {
        try {
            categoryCombo.setItems(FXCollections.observableArrayList(categoryService.list(null)));
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao carregar categorias", exception);
        }
    }

    private void fillForm(RecurringAccountEntity account) {
        if (account == null) {
            clearForm();
            return;
        }
        descriptionField.setText(account.getDescription());
        typeCombo.setValue(account.getType());
        amountField.setText(account.getAmount() == null ? "" : account.getAmount().toPlainString().replace('.', ','));
        categoryCombo.setValue(account.getCategory());
        frequencyCombo.setValue(account.getFrequency());
        intervalField.setText(account.getCustomIntervalDays() == null ? "" : String.valueOf(account.getCustomIntervalDays()));
        nextOccurrencePicker.setValue(account.getNextOccurrence());
        notesArea.setText(account.getNotes());
        activeCheck.setSelected(account.isActive());
        saveButton.setText("Atualizar");
    }

    private void clearForm() {
        selected = null;
        descriptionField.clear();
        typeCombo.setValue(MovementType.DESPESA);
        amountField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        frequencyCombo.setValue(RecurrenceFrequency.MENSAL);
        intervalField.clear();
        nextOccurrencePicker.setValue(LocalDate.now());
        notesArea.clear();
        activeCheck.setSelected(true);
        saveButton.setText("Salvar");
    }

    private static Integer parseInterval(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Intervalo em dias inválido.", exception);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static StringConverter<CategoryEntity> categoryConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(CategoryEntity category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public CategoryEntity fromString(String string) {
                return null;
            }
        };
    }
}
