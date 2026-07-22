package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import model.MovementType;
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
            "Backup"
    );

    private final List<String> icons = List.of(
            "🏠",
            "📂",
            "💰",
            "💸",
            "🔁",
            "💳",
            "🎯",
            "📊",
            "☁"
    );

    @FXML
    private void initialize() {
        navButtons = List.of(
                dashboardNav, categoriesNav, incomeNav, expenseNav,
                recurringNav, cardsNav, goalsNav, reportsNav, backupNav);
        showDashboard();
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

            if (expanded) {
                navButtons.get(i).setText(icons.get(i));
            } else {
                navButtons.get(i).setText(icons.get(i) + " " + texts.get(i));
            }

        }
        expanded = !expanded;
    }
}
