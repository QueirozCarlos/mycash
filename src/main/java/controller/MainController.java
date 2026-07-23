package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import model.MovementType;
import util.IconUtils;
import util.UiDialogs;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class MainController {

    @FXML
    private StackPane contentHost;

    @FXML
    private Button settingsButton;

    @FXML
    private Button dashboardNav;

    @FXML
    private Button categoriesNav;

    @FXML
    private Button incomeNav;

    @FXML
    private Button expenseNav;

    @FXML
    private Button recurringNav;

    @FXML
    private Button cardsNav;

    @FXML
    private Button goalsNav;

    @FXML
    private Button reportsNav;

    @FXML
    private Button backupNav;

    @FXML
    private VBox sidebar;

    private boolean expanded = true;

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;


    private MovementType currentMovementType = MovementType.RECEITA;
    private List<Button> navButtons;

    private final List<String> texts = List.of(
            "Dashboard",
            "Categorias",
            "Receitas",
            "Despesas",
            "Recorrentes",
            "Cartões",
            "Metas",
            "Relatórios",
            "Backup",
            "Configurações"
    );


    @FXML
    private void initialize() {
        navButtons = List.of(
                dashboardNav, categoriesNav, incomeNav, expenseNav,
                recurringNav, cardsNav, goalsNav, reportsNav, backupNav, settingsButton);
        showDashboard();
    
        dashboardNav.setGraphic(IconUtils.load("home.png"));
        categoriesNav.setGraphic(IconUtils.load("category.png"));
        incomeNav.setGraphic(IconUtils.load("income.png"));
        expenseNav.setGraphic(IconUtils.load("expense.png"));
        recurringNav.setGraphic(IconUtils.load("recurring.png"));
        cardsNav.setGraphic(IconUtils.load("card.png"));
        goalsNav.setGraphic(IconUtils.load("goal.png"));
        reportsNav.setGraphic(IconUtils.load("flag.png"));
        backupNav.setGraphic(IconUtils.load("backup.png"));
        settingsButton.setGraphic(IconUtils.load("settings.png"));
        }
    @FXML
    private void showDashboard() {
        show("dashboard-view.fxml", dashboardNav, null);
    }

    @FXML
    private void showCategories() {
        show("categories-view.fxml", categoriesNav, null);
    }

    @FXML
    private void showIncome() {
        currentMovementType = MovementType.RECEITA;
        show("movements-view.fxml", incomeNav, currentMovementType);
    }

    @FXML
    private void showExpense() {
        currentMovementType = MovementType.DESPESA;
        show("movements-view.fxml", expenseNav, currentMovementType);
    }

    @FXML
    private void showRecurring() {
        show("recurring-view.fxml", recurringNav, null);
    }

    @FXML
    private void showCards() {
        show("cards-view.fxml", cardsNav, null);
    }

    @FXML
    private void showGoals() {
        show("goals-view.fxml", goalsNav, null);
    }

    @FXML
    private void showReports() {
        show("reports-view.fxml", reportsNav, null);
    }

    @FXML
    private void showBackup() {
        show("backup-view.fxml", backupNav, null);
    }

    @FXML
    private void showSettings() {
        show("settings-view.fxml", null, null);
    }

    private void show(String fxml, Button nav, MovementType movementType) {
        try {
            highlight(nav);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
            Parent root = loader.load();
            if (loader.getController() instanceof MovementsController mc) {
                mc.setType(movementType != null ? movementType : currentMovementType);
            }
            if (root instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            StackPane.setAlignment(root, Pos.TOP_LEFT);
            contentHost.getChildren().setAll(root);
        } catch (IOException exception) {
            UiDialogs.error("Falha ao carregar a tela " + fxml, exception);
        }
    }

    private void highlight(Button active) {
        for (Button button : navButtons) {
            button.getStyleClass().remove("active");
        }
        if (active != null && !active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    private void animateSidebar(double width) {

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(250),
                        new KeyValue(sidebar.prefWidthProperty(), width)
                )
        );

        timeline.play();
    }

    @FXML
    private void toggleSidebar() {

        animateSidebar(expanded ? 70 : 240);

        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setText(expanded ? "" : texts.get(i));
        }
        expanded = !expanded;

        titleLabel.setText(expanded ? "Financeiro" : "");
        subtitleLabel.setText(expanded ? "Gestão financeira" : "");

    }
}
