package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import service.DataExchangeService;
import service.ReportService;
import util.UiDialogs;

import java.io.File;
import java.nio.file.Path;

public class ReportsController {

    @FXML
    private Label statusLabel;

    private final ReportService reportService = new ReportService();
    private final DataExchangeService dataExchangeService = new DataExchangeService();

    @FXML
    private void initialize() {
        statusLabel.setText("Selecione uma exportação para começar.");
    }

    @FXML
    private void onExportCashFlowCsv() {
        File target = chooseSaveFile("fluxo-caixa.csv", "CSV", "*.csv");
        if (target == null) {
            return;
        }
        try {
            Path path = reportService.exportCashFlowCsv(target.toPath());
            statusLabel.setText("Arquivo salvo em: " + path);
            UiDialogs.info("Fluxo de caixa exportado com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao exportar fluxo de caixa (CSV)", exception);
        }
    }

    @FXML
    private void onExportMovementsCsv() {
        File target = chooseSaveFile("movimentacoes.csv", "CSV", "*.csv");
        if (target == null) {
            return;
        }
        try {
            Path path = reportService.exportMovementsCsv(target.toPath(), null);
            statusLabel.setText("Arquivo salvo em: " + path);
            UiDialogs.info("Movimentações exportadas com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao exportar movimentações (CSV)", exception);
        }
    }

    @FXML
    private void onExportCashFlowPdf() {
        File target = chooseSaveFile("fluxo-caixa.pdf", "PDF", "*.pdf");
        if (target == null) {
            return;
        }
        try {
            Path path = reportService.exportCashFlowPdf(target.toPath());
            statusLabel.setText("Arquivo salvo em: " + path);
            UiDialogs.info("PDF de fluxo de caixa exportado com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao exportar fluxo de caixa (PDF)", exception);
        }
    }

    @FXML
    private void onExportJson() {
        File target = chooseSaveFile("financeiro-dados.json", "JSON", "*.json");
        if (target == null) {
            return;
        }
        try {
            Path path = dataExchangeService.exportJson(target.toPath());
            statusLabel.setText("JSON exportado em: " + path);
            UiDialogs.info("Dados exportados em JSON com sucesso.");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao exportar JSON", exception);
        }
    }

    @FXML
    private void onImportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        Window window = statusLabel.getScene() == null ? null : statusLabel.getScene().getWindow();
        File source = chooser.showOpenDialog(window);
        if (source == null) {
            return;
        }
        try {
            int imported = dataExchangeService.importJson(source.toPath());
            statusLabel.setText("Importados " + imported + " lançamento(s).");
            UiDialogs.info("Importação concluída: " + imported + " lançamento(s).");
        } catch (RuntimeException exception) {
            UiDialogs.error("Erro ao importar JSON", exception);
        }
    }

    private File chooseSaveFile(String initialName, String description, String extension) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar relatório");
        chooser.setInitialFileName(initialName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        Window window = statusLabel.getScene() == null ? null : statusLabel.getScene().getWindow();
        return chooser.showSaveDialog(window);
    }
}
