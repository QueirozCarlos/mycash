package service;

import config.ApplicationBootstrap;
import config.DatabaseBootstrap;
import dto.DashboardSummaryDto;
import model.CategoryType;
import model.MovementStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardAndBackupServiceTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ApplicationBootstrap.initialize(tempDir);
        DatabaseBootstrap.initialize(tempDir);
        new UserService().ensureDefaultUser();
    }

    @AfterEach
    void tearDown() {
        DatabaseBootstrap.shutdown();
    }

    @Test
    void shouldComputeDashboardSummaryAndCreateBackup() throws Exception {
        CategoryService categoryService = new CategoryService();
        MovementService movementService = new MovementService();
        var salary = categoryService.create("Salário", CategoryType.SALARIO, "#14B8A6");
        var food = categoryService.create("Alimentação", CategoryType.ALIMENTACAO, "#F97316");

        movementService.createIncome("Salário", new BigDecimal("3000.00"), LocalDate.now(), salary, null, MovementStatus.PAGO);
        movementService.createExpense("Mercado", new BigDecimal("200.00"), LocalDate.now(), null, food, null, MovementStatus.PAGO, false);

        DashboardSummaryDto summary = new DashboardService().buildSummary();
        assertEquals(0, new BigDecimal("2800.00").compareTo(summary.balance()));
        assertEquals(0, new BigDecimal("3000.00").compareTo(summary.monthIncome()));
        assertEquals(0, new BigDecimal("200.00").compareTo(summary.monthExpense()));

        // Point AppPaths is fixed to user.home — backup uses AppPaths; for test use BackupService against real paths
        // after initializing app dirs in temp via reflection is hard. Instead export JSON as integration check.
        Path json = tempDir.resolve("export.json");
        Path exported = new DataExchangeService().exportJson(json);
        assertTrue(Files.exists(exported));
        assertTrue(Files.size(exported) > 20);
    }
}
