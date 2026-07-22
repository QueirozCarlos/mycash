package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import service.ApplicationLifecycleService;
import service.BackupService;
import service.ConfigurationService;
import util.UiDialogs;

import java.nio.file.Path;

public class BackupController {

    @FXML
    private ListView<Path> backupList;

    @FXML
    private Label statusLabel;

    @FXML
    private CheckBox autoBackupCheck;

    private final BackupService backupService = new BackupService();
    private final ConfigurationService configurationService = new ConfigurationService();
    private final ObservableList<Path> items = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        backupList.setItems(items);
        backupList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFileName().toString());
            }
        });

        boolean enabled = configurationService.get(ApplicationLifecycleService.AUTO_BACKUP_KEY)
                .map(value -> "true".equalsIgnoreCase(value) || "1".equals(value))
                .orElse(false);
        autoBackupCheck.setSelected(enabled);
        refresh();
    }

    @FXML
    private void onToggleAutoBackup() {
        try {
            configurationService.set(
                    ApplicationLifecycleService.AUTO_BACKUP_KEY,
                    autoBackupCheck.isSelected() ? "true" : "false");
            statusLabel.setText(autoBackupCheck.isSelected()
                    ? "Backup automático ativado."
                    : "Backup automático desativado.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao salvar preferência", exception);
        }
    }

    @FXML
    private void onCreateBackup() {
        try {
            Path created = backupService.createBackup();
            refresh();
            statusLabel.setText("Backup criado: " + created.getFileName());
            UiDialogs.info("Backup criado com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao criar backup", exception);
        }
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onRestore() {
        Path selected = backupList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            UiDialogs.error("Selecione um backup para restaurar.");
            return;
        }
        if (!UiDialogs.confirm(
                "Restaurar o backup \"" + selected.getFileName()
                        + "\"? Os dados atuais serão substituídos.")) {
            return;
        }
        try {
            backupService.restoreBackup(selected);
            statusLabel.setText("Backup restaurado: " + selected.getFileName());
            UiDialogs.info("Backup restaurado com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao restaurar backup", exception);
        }
    }

    private void refresh() {
        try {
            items.setAll(backupService.listBackups());
            statusLabel.setText(items.isEmpty()
                    ? "Nenhum backup encontrado."
                    : items.size() + " backup(s) disponível(is).");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao listar backups", exception);
        }
    }
}
