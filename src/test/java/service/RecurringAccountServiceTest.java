package service;

import config.ApplicationBootstrap;
import config.DatabaseBootstrap;
import model.CategoryEntity;
import model.CategoryType;
import model.MovementType;
import model.RecurrenceFrequency;
import model.RecurringAccountEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecurringAccountServiceTest {

    @TempDir
    Path tempDir;

    private RecurringAccountService recurringAccountService;
    private MovementService movementService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        ApplicationBootstrap.initialize(tempDir);
        DatabaseBootstrap.initialize(tempDir);
        new UserService().ensureDefaultUser();
        recurringAccountService = new RecurringAccountService();
        movementService = new MovementService();
        categoryService = new CategoryService();
    }

    @AfterEach
    void tearDown() {
        DatabaseBootstrap.shutdown();
    }

    @Test
    void shouldGenerateExpenseWhenRecurringIsDue() {
        CategoryEntity category = categoryService.create("Academia", CategoryType.LAZER, "#EC4899");

        RecurringAccountEntity account = recurringAccountService.create(
                "Mensalidade academia",
                MovementType.DESPESA,
                new BigDecimal("149.00"),
                category,
                RecurrenceFrequency.MENSAL,
                null,
                LocalDate.now(),
                null);

        assertEquals(1, movementService.listByType(MovementType.DESPESA).size());
        assertTrue(account.getNextOccurrence().isAfter(LocalDate.now()));
        assertEquals(0, recurringAccountService.generateDueOccurrences());
        assertEquals(1, movementService.listByType(MovementType.DESPESA).size());
    }
}
