package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ApplicationLifecycleService;
import util.ThemeManager;

import java.io.IOException;
import java.util.Locale;

public class MainApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    private final ApplicationLifecycleService applicationLifecycleService = new ApplicationLifecycleService();

    @Override
    public void start(Stage primaryStage) throws IOException {
        Locale.setDefault(Locale.of("pt", "BR"));
        applicationLifecycleService.start();

        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/fxml/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1360, 860);
        ThemeManager.bind(scene);

        primaryStage.setTitle("Financeiro");
        primaryStage.setMinWidth(1180);
        primaryStage.setMinHeight(760);
        primaryStage.setScene(scene);
        primaryStage.show();
        LOGGER.info("Interface iniciada. Tema: {}", ThemeManager.current());
    }

    @Override
    public void stop() {
        applicationLifecycleService.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
