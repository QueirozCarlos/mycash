package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import model.CategoryEntity;
import model.CategoryType;
import service.CategoryService;
import util.ColorUtils;
import util.UiDialogs;

public class CategoriesController {

    @FXML
    private TextField searchField;

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<CategoryType> typeCombo;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private CheckBox activeCheck;

    @FXML
    private TableView<CategoryEntity> table;

    @FXML
    private TableColumn<CategoryEntity, Long> idColumn;

    @FXML
    private TableColumn<CategoryEntity, String> nameColumn;

    @FXML
    private TableColumn<CategoryEntity, CategoryType> typeColumn;

    @FXML
    private TableColumn<CategoryEntity, String> colorColumn;

    @FXML
    private TableColumn<CategoryEntity, Boolean> activeColumn;

    @FXML
    private Button saveButton;

    @FXML
    private Label statusLabel;

    private final CategoryService categoryService = new CategoryService();
    private final ObservableList<CategoryEntity> items = FXCollections.observableArrayList();
    private CategoryEntity selected;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        colorColumn.setCellValueFactory(new PropertyValueFactory<>("colorHex"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        colorColumn.setCellFactory(column -> new TableCell<>() {
            private final Region swatch = new Region();

            {
                swatch.setMinSize(28, 18);
                swatch.setPrefSize(28, 18);
                swatch.setMaxSize(28, 18);
                setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                String hex = ColorUtils.toHex(ColorUtils.fromHex(item));
                swatch.setStyle(ColorUtils.css(hex)
                        + " -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #94A3B8; -fx-border-width: 1;");
                setGraphic(swatch);
                setText(null);
            }
        });

        typeCombo.setItems(FXCollections.observableArrayList(CategoryType.values()));
        typeCombo.getSelectionModel().select(CategoryType.OUTROS);
        colorPicker.setValue(Color.web("#5B6CFF"));
        activeCheck.setSelected(true);

        table.setItems(items);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selected = newValue;
            fillForm(newValue);
        });

        refresh();
        statusLabel.setText("Pronto para cadastrar, editar e excluir categorias.");
    }

    @FXML
    private void onSearch() {
        refresh();
    }

    @FXML
    private void onSave() {
        try {
            String colorHex = ColorUtils.toHex(colorPicker.getValue());
            CategoryEntity persisted;
            if (selected == null) {
                persisted = categoryService.create(
                        nameField.getText(),
                        typeCombo.getValue(),
                        colorHex);
            } else {
                persisted = categoryService.update(
                        selected.getId(),
                        nameField.getText(),
                        typeCombo.getValue(),
                        colorHex,
                        activeCheck.isSelected());
            }
            selected = persisted;
            refresh();
            table.getSelectionModel().select(persisted);
            statusLabel.setText("Categoria salva com sucesso.");
            UiDialogs.info("Categoria salva com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao salvar categoria", exception);
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) {
            UiDialogs.error("Selecione uma categoria para excluir.");
            return;
        }
        if (!UiDialogs.confirm("Excluir a categoria \"" + selected.getName() + "\"?")) {
            return;
        }
        try {
            categoryService.delete(selected.getId());
            clearForm();
            refresh();
            statusLabel.setText("Categoria excluída.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao excluir categoria", exception);
        }
    }

    @FXML
    private void onClear() {
        clearForm();
        table.getSelectionModel().clearSelection();
        statusLabel.setText("Formulário limpo.");
    }

    private void refresh() {
        try {
            items.setAll(categoryService.list(searchField.getText()));
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao listar categorias", exception);
        }
    }

    private void fillForm(CategoryEntity category) {
        if (category == null) {
            clearForm();
            return;
        }
        nameField.setText(category.getName());
        typeCombo.setValue(category.getType());
        colorPicker.setValue(ColorUtils.fromHex(category.getColorHex()));
        activeCheck.setSelected(category.isActive());
        saveButton.setText("Atualizar");
    }

    private void clearForm() {
        selected = null;
        nameField.clear();
        typeCombo.getSelectionModel().select(CategoryType.OUTROS);
        colorPicker.setValue(Color.web("#5B6CFF"));
        activeCheck.setSelected(true);
        saveButton.setText("Salvar");
    }
}
