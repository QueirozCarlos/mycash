package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class UiDialogs {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiDialogs.class);

    private UiDialogs() {
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Financeiro", message);
    }

    public static void error(String title, Throwable error) {
        LOGGER.error(title, error);
        String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        show(Alert.AlertType.ERROR, title, message);
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Erro", message);
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Confirmação");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
