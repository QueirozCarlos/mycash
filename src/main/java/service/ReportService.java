package service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import exception.ApplicationException;
import model.MovementEntity;
import model.MovementType;
import repository.JpaMovementRepository;
import repository.MovementRepository;
import util.Formatters;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class ReportService {

    private final MovementRepository movementRepository;
    private final DashboardService dashboardService;

    public ReportService() {
        this(new JpaMovementRepository(), new DashboardService());
    }

    ReportService(MovementRepository movementRepository, DashboardService dashboardService) {
        this.movementRepository = movementRepository;
        this.dashboardService = dashboardService;
    }

    public Path exportCashFlowCsv(Path target) {
        YearMonth current = YearMonth.now();
        StringBuilder csv = new StringBuilder();
        csv.append("Mes;Receitas;Despesas;Saldo\n");

        for (var comparison : dashboardService.monthlyComparison(6)) {
            BigDecimal balance = comparison.income().subtract(comparison.expense());
            csv.append(escapeCsv(comparison.yearMonth())).append(';')
                    .append(formatAmount(comparison.income())).append(';')
                    .append(formatAmount(comparison.expense())).append(';')
                    .append(formatAmount(balance)).append('\n');
        }

        // Include current month totals explicitly if needed for clarity
        LocalDate start = current.atDay(1);
        LocalDate end = current.atEndOfMonth();
        List<MovementEntity> monthMovements = movementRepository.findByPeriod(start, end);
        csv.append('\n').append("Movimentacoes do mes atual (").append(current).append("): ")
                .append(monthMovements.size()).append('\n');

        return writeText(target, csv.toString());
    }

    public Path exportMovementsCsv(Path target, MovementType typeOptional) {
        List<MovementEntity> movements = typeOptional == null
                ? movementRepository.findAll()
                : movementRepository.findByType(typeOptional);

        StringBuilder csv = new StringBuilder();
        csv.append("Id;Tipo;Descricao;Valor;Data;Vencimento;Status;Categoria;Observacoes\n");

        for (MovementEntity movement : movements) {
            csv.append(movement.getId()).append(';')
                    .append(movement.getType()).append(';')
                    .append(escapeCsv(movement.getDescription())).append(';')
                    .append(formatAmount(movement.getAmount())).append(';')
                    .append(Formatters.formatDate(movement.getMovementDate())).append(';')
                    .append(Formatters.formatDate(movement.getDueDate())).append(';')
                    .append(movement.getStatus()).append(';')
                    .append(escapeCsv(movement.getCategoryName())).append(';')
                    .append(escapeCsv(movement.getNotes() == null ? "" : movement.getNotes()))
                    .append('\n');
        }

        return writeText(target, csv.toString());
    }

    public Path exportCashFlowPdf(Path target) {
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }

            try (OutputStream outputStream = Files.newOutputStream(target)) {
                Document document = new Document();
                PdfWriter.getInstance(document, outputStream);
                document.open();

                Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

                document.add(new Paragraph("Fluxo de Caixa - Ultimos 6 meses", titleFont));
                document.add(new Paragraph(" "));

                var summary = dashboardService.buildSummary();
                document.add(new Paragraph("Saldo atual: " + Formatters.formatCurrency(summary.balance()), bodyFont));
                document.add(new Paragraph("Receitas do mes: " + Formatters.formatCurrency(summary.monthIncome()), bodyFont));
                document.add(new Paragraph("Despesas do mes: " + Formatters.formatCurrency(summary.monthExpense()), bodyFont));
                document.add(new Paragraph("Economia do mes: " + Formatters.formatCurrency(summary.monthSavings()), bodyFont));
                document.add(new Paragraph(" "));
                document.add(new Paragraph("Comparativo mensal:", titleFont));
                document.add(new Paragraph(" "));

                for (var comparison : dashboardService.monthlyComparison(6)) {
                    BigDecimal balance = comparison.income().subtract(comparison.expense());
                    document.add(new Paragraph(
                            comparison.yearMonth()
                                    + " | Receitas: " + Formatters.formatCurrency(comparison.income())
                                    + " | Despesas: " + Formatters.formatCurrency(comparison.expense())
                                    + " | Saldo: " + Formatters.formatCurrency(balance),
                            bodyFont));
                }

                document.close();
            }

            return target;
        } catch (IOException | DocumentException exception) {
            throw new ApplicationException("Falha ao exportar PDF de fluxo de caixa.", exception);
        }
    }

    private Path writeText(Path target, String content) {
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.writeString(target, content, StandardCharsets.UTF_8);
            return target;
        } catch (IOException exception) {
            throw new ApplicationException("Falha ao exportar CSV para " + target, exception);
        }
    }

    private static String formatAmount(BigDecimal amount) {
        return amount == null ? "0,00" : amount.toPlainString().replace('.', ',');
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
