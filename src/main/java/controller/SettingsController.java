package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import service.ApplicationLifecycleService;
import service.ConfigurationService;
import util.ThemeManager;
import util.UiDialogs;

public class SettingsController {

    @FXML
    private Button darkThemeButton;

    @FXML
    private Button lightThemeButton;

    @FXML
    private Label themeStatusLabel;

    @FXML
    private CheckBox autoBackupCheck;

    @FXML
    private Label backupStatusLabel;

    private final ConfigurationService configurationService = new ConfigurationService();

    @FXML
    private void initialize() {
        refreshThemeButtons();
        boolean enabled = configurationService.get(ApplicationLifecycleService.AUTO_BACKUP_KEY)
                .map(value -> "true".equalsIgnoreCase(value) || "1".equals(value))
                .orElse(false);
        autoBackupCheck.setSelected(enabled);
        backupStatusLabel.setText(enabled
                ? "Backup automático ativado."
                : "Backup automático desativado.");
    }

    @FXML
    private void onSelectDark() {
        ThemeManager.setTheme(ThemeManager.Theme.DARK);
        refreshThemeButtons();
    }

    @FXML
    private void onSelectLight() {
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
        refreshThemeButtons();
    }

    @FXML
    private void onToggleAutoBackup() {
        try {
            configurationService.set(
                    ApplicationLifecycleService.AUTO_BACKUP_KEY,
                    autoBackupCheck.isSelected() ? "true" : "false");
            backupStatusLabel.setText(autoBackupCheck.isSelected()
                    ? "Backup automático ativado."
                    : "Backup automático desativado.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao salvar preferência", exception);
        }
    }

    private void refreshThemeButtons() {
        darkThemeButton.getStyleClass().remove("active");
        lightThemeButton.getStyleClass().remove("active");
        if (ThemeManager.isDark()) {
            darkThemeButton.getStyleClass().add("active");
            themeStatusLabel.setText("Tema atual: Escuro");
        } else {
            lightThemeButton.getStyleClass().add("active");
            themeStatusLabel.setText("Tema atual: Claro");
        }
    }
}
