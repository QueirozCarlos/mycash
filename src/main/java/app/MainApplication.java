package app;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ApplicationLifecycleService;
import util.IconUtils;
import util.ThemeManager;

import java.util.Locale;

public class MainApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    private final ApplicationLifecycleService applicationLifecycleService = new ApplicationLifecycleService();

    @Override
    public void start(Stage primaryStage) {
        Locale.setDefault(Locale.of("pt", "BR"));

        // 1. Descobre o tema salvo de forma ultra-rápida (sem banco)
        ThemeManager.Theme currentTheme = ThemeManager.getSavedThemeFast();
        boolean isDark = currentTheme == ThemeManager.Theme.DARK;

        // 2. Monta os componentes do Splash
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(48, 48);

        Label label = new Label("Iniciando o My Cash...");
        label.setStyle(isDark
                ? "-fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-font-weight: bold;"
                : "-fx-text-fill: #2b2b2b; -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox loadingLayout = new VBox(18, progress, label);
        loadingLayout.setAlignment(Pos.CENTER);

        // Define o fundo do Splash: Claro (#f8f9fa) ou Escuro (#1e1e2e)
        loadingLayout.setStyle(isDark
                ? "-fx-background-color: #1e1e2e;"
                : "-fx-background-color: #ffffff;");

        Scene loadingScene = new Scene(loadingLayout, 1360, 860);

        // Aplica o tema na cena do Splash
        ThemeManager.bind(loadingScene);

        primaryStage.setTitle("My Cash");
        primaryStage.setScene(loadingScene);

        // Ícone da Janela
        try {
            Image icon = IconUtils.loadImage("app-icon.png");
            if (icon != null) {
                primaryStage.getIcons().add(icon);
            }
        } catch (Exception e) {
            LOGGER.warn("Não foi possível carregar o ícone da janela: {}", e.getMessage());
        }

        // Janela abre instantaneamente no tema correto!
        primaryStage.show();

        // 3. Task de inicialização em segundo plano (Flyway + Hibernate + UI Principal)
        Task<Scene> initTask = new Task<>() {
            @Override
            protected Scene call() throws Exception {
                // Sobe o Banco/Hibernate em background
                applicationLifecycleService.start();

                // Carrega o FXML principal
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/fxml/main-view.fxml"));
                Scene mainScene = new Scene(loader.load(), 1360, 860);

                return mainScene;
            }
        };

        // 4. Sucesso na subida do banco
        initTask.setOnSucceeded(e -> {
            Scene mainScene = initTask.getValue();

            // Re-vincula a cena principal no ThemeManager
            ThemeManager.bind(mainScene);

            primaryStage.setScene(mainScene);
            primaryStage.setMinWidth(1180);
            primaryStage.setMinHeight(760);
            primaryStage.centerOnScreen();
            LOGGER.info("Interface iniciada com sucesso. Tema: {}", ThemeManager.current());
        });

        // 5. Trata erro de carregamento
        initTask.setOnFailed(e -> {
            Throwable exception = initTask.getException();
            LOGGER.error("Falha na inicialização do aplicativo", exception);
            progress.setVisible(false);
            label.setText("Erro ao carregar o banco de dados. Verifique os logs.");
            label.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px;");
        });

        Thread initThread = new Thread(initTask, "app-init-thread");
        initThread.setDaemon(true);
        initThread.start();
    }

    @Override
    public void stop() {
        applicationLifecycleService.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}