package controller;

import dto.CreditCardSummaryDto;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import model.CategoryEntity;
import model.CreditCardEntity;
import model.InstallmentPlanEntity;
import service.CategoryService;
import service.CreditCardService;
import service.InstallmentPlanService;
import util.Formatters;
import util.UiDialogs;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CardsController {

    @FXML
    private TableView<CreditCardSummaryDto> cardsTable;

    @FXML
    private TableColumn<CreditCardSummaryDto, String> cardNameColumn;

    @FXML
    private TableColumn<CreditCardSummaryDto, BigDecimal> limitColumn;

    @FXML
    private TableColumn<CreditCardSummaryDto, BigDecimal> usedColumn;

    @FXML
    private TableColumn<CreditCardSummaryDto, BigDecimal> availableColumn;

    @FXML
    private TableColumn<CreditCardSummaryDto, Number> closingDayColumn;

    @FXML
    private TableColumn<CreditCardSummaryDto, Number> dueDayColumn;

    @FXML
    private TextField cardNameField;

    @FXML
    private TextField limitField;

    @FXML
    private TextField closingDayField;

    @FXML
    private TextField dueDayField;

    @FXML
    private CheckBox cardActiveCheck;

    @FXML
    private Button saveCardButton;

    @FXML
    private TableView<InstallmentPlanEntity> plansTable;

    @FXML
    private TableColumn<InstallmentPlanEntity, String> planDescriptionColumn;

    @FXML
    private TableColumn<InstallmentPlanEntity, String> planCardColumn;

    @FXML
    private TableColumn<InstallmentPlanEntity, BigDecimal> planTotalColumn;

    @FXML
    private TableColumn<InstallmentPlanEntity, Number> planPaidColumn;

    @FXML
    private TableColumn<InstallmentPlanEntity, Number> planPendingColumn;

    @FXML
    private TableColumn<InstallmentPlanEntity, BigDecimal> planRemainingColumn;

    @FXML
    private ComboBox<CreditCardEntity> planCardCombo;

    @FXML
    private TextField planDescriptionField;

    @FXML
    private TextField planTotalField;

    @FXML
    private TextField planCountField;

    @FXML
    private DatePicker planStartPicker;

    @FXML
    private ComboBox<CategoryEntity> planCategoryCombo;

    private final CreditCardService creditCardService = new CreditCardService();
    private final InstallmentPlanService installmentPlanService = new InstallmentPlanService();
    private final CategoryService categoryService = new CategoryService();

    private final ObservableList<CreditCardSummaryDto> cardItems = FXCollections.observableArrayList();
    private final ObservableList<InstallmentPlanEntity> planItems = FXCollections.observableArrayList();

    private CreditCardEntity selectedCard;
    private InstallmentPlanEntity selectedPlan;

    @FXML
    private void initialize() {
        cardNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().card().getName()));
        limitColumn.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().card().getCreditLimit()));
        usedColumn.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().usedAmount()));
        availableColumn.setCellValueFactory(cell ->
                new SimpleObjectProperty<>(cell.getValue().availableLimit()));
        closingDayColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().card().getClosingDay()));
        dueDayColumn.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().card().getDueDay()));

        limitColumn.setCellFactory(column -> currencySummaryCell());
        usedColumn.setCellFactory(column -> currencySummaryCell());
        availableColumn.setCellFactory(column -> currencySummaryCell());

        planDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        planCardColumn.setCellValueFactory(new PropertyValueFactory<>("creditCardName"));
        planTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        planPaidColumn.setCellValueFactory(new PropertyValueFactory<>("paidInstallments"));
        planPendingColumn.setCellValueFactory(new PropertyValueFactory<>("pendingInstallments"));
        planRemainingColumn.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));

        planTotalColumn.setCellFactory(column -> currencyPlanCell());
        planRemainingColumn.setCellFactory(column -> currencyPlanCell());

        planCardCombo.setConverter(cardConverter());
        planCategoryCombo.setConverter(categoryConverter());

        cardsTable.setItems(cardItems);
        cardsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        cardsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedCard = newValue == null ? null : newValue.card();
            fillCardForm(selectedCard);
        });

        plansTable.setItems(planItems);
        plansTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        plansTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
                selectedPlan = newValue);

        planStartPicker.setValue(LocalDate.now());
        cardActiveCheck.setSelected(true);
        loadLookups();
        refreshCards();
        refreshPlans();
    }

    @FXML
    private void onSaveCard() {
        try {
            BigDecimal limit = Formatters.parseCurrency(limitField.getText());
            int closingDay = Integer.parseInt(closingDayField.getText().trim());
            int dueDay = Integer.parseInt(dueDayField.getText().trim());

            CreditCardEntity persisted;
            if (selectedCard == null) {
                persisted = creditCardService.create(cardNameField.getText(), limit, closingDay, dueDay);
            } else {
                persisted = creditCardService.update(
                        selectedCard.getId(),
                        cardNameField.getText(),
                        limit,
                        closingDay,
                        dueDay,
                        cardActiveCheck.isSelected());
            }
            selectedCard = persisted;
            refreshCards();
            refreshPlanCards();
            selectCard(persisted);
            UiDialogs.info("Cartão salvo com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao salvar cartão", exception);
        }
    }

    @FXML
    private void onDeleteCard() {
        if (selectedCard == null) {
            UiDialogs.error("Selecione um cartão para excluir.");
            return;
        }
        if (!UiDialogs.confirm("Excluir o cartão \"" + selectedCard.getName() + "\"?")) {
            return;
        }
        try {
            creditCardService.delete(selectedCard.getId());
            clearCardForm();
            refreshCards();
            refreshPlanCards();
            refreshPlans();
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao excluir cartão", exception);
        }
    }

    @FXML
    private void onClearCard() {
        clearCardForm();
        cardsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onCreatePlan() {
        try {
            int count = Integer.parseInt(planCountField.getText().trim());
            installmentPlanService.create(
                    planCardCombo.getValue(),
                    planDescriptionField.getText(),
                    Formatters.parseCurrency(planTotalField.getText()),
                    count,
                    planStartPicker.getValue(),
                    planCategoryCombo.getValue());
            planDescriptionField.clear();
            planTotalField.clear();
            planCountField.clear();
            planStartPicker.setValue(LocalDate.now());
            refreshPlans();
            refreshCards();
            UiDialogs.info("Parcelamento criado com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao criar parcelamento", exception);
        }
    }

    @FXML
    private void onMarkNextPaid() {
        if (selectedPlan == null) {
            UiDialogs.error("Selecione um parcelamento.");
            return;
        }
        try {
            installmentPlanService.markNextPaid(selectedPlan.getId());
            refreshPlans();
            refreshCards();
            UiDialogs.info("Parcela marcada como paga.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao marcar parcela", exception);
        }
    }

    @FXML
    private void onDeletePlan() {
        if (selectedPlan == null) {
            UiDialogs.error("Selecione um parcelamento para excluir.");
            return;
        }
        if (!UiDialogs.confirm("Excluir o parcelamento \"" + selectedPlan.getDescription() + "\"?")) {
            return;
        }
        try {
            installmentPlanService.delete(selectedPlan.getId());
            selectedPlan = null;
            refreshPlans();
            refreshCards();
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao excluir parcelamento", exception);
        }
    }

    private void refreshCards() {
        try {
            cardItems.setAll(creditCardService.listSummaries());
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao listar cartões", exception);
        }
    }

    private void refreshPlans() {
        try {
            planItems.setAll(installmentPlanService.list());
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao listar parcelamentos", exception);
        }
    }

    private void loadLookups() {
        try {
            planCategoryCombo.setItems(FXCollections.observableArrayList(categoryService.list(null)));
            refreshPlanCards();
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao carregar dados auxiliares", exception);
        }
    }

    private void refreshPlanCards() {
        try {
            planCardCombo.setItems(FXCollections.observableArrayList(creditCardService.list()));
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao carregar cartões", exception);
        }
    }

    private void fillCardForm(CreditCardEntity card) {
        if (card == null) {
            clearCardForm();
            return;
        }
        cardNameField.setText(card.getName());
        limitField.setText(card.getCreditLimit().toPlainString().replace('.', ','));
        closingDayField.setText(String.valueOf(card.getClosingDay()));
        dueDayField.setText(String.valueOf(card.getDueDay()));
        cardActiveCheck.setSelected(card.isActive());
        saveCardButton.setText("Atualizar");
    }

    private void clearCardForm() {
        selectedCard = null;
        cardNameField.clear();
        limitField.clear();
        closingDayField.clear();
        dueDayField.clear();
        cardActiveCheck.setSelected(true);
        saveCardButton.setText("Salvar");
    }

    private void selectCard(CreditCardEntity card) {
        for (CreditCardSummaryDto summary : cardItems) {
            if (summary.card().getId().equals(card.getId())) {
                cardsTable.getSelectionModel().select(summary);
                break;
            }
        }
    }

    private static TableCell<CreditCardSummaryDto, BigDecimal> currencySummaryCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatCurrency(item));
            }
        };
    }

    private static TableCell<InstallmentPlanEntity, BigDecimal> currencyPlanCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Formatters.formatCurrency(item));
            }
        };
    }

    private static StringConverter<CreditCardEntity> cardConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(CreditCardEntity card) {
                return card == null ? "" : card.getName();
            }

            @Override
            public CreditCardEntity fromString(String string) {
                return null;
            }
        };
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
