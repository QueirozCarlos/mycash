package service;

import config.ApplicationBootstrap;
import config.DatabaseBootstrap;
import model.CategoryEntity;
import model.CategoryType;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovementServiceTest {

    @TempDir
    Path tempDir;

    private MovementService movementService;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        ApplicationBootstrap.initialize(tempDir);
        DatabaseBootstrap.initialize(tempDir);
        movementService = new MovementService();
        categoryService = new CategoryService();
        new UserService().ensureDefaultUser();
    }

    @AfterEach
    void tearDown() {
        DatabaseBootstrap.shutdown();
    }

    @Test
    void shouldCreateSearchAndDeleteIncomeAndExpense() {
        CategoryEntity salary = categoryService.create("Salário Teste", CategoryType.SALARIO, "#14B8A6");
        CategoryEntity food = categoryService.create("Mercado Teste", CategoryType.ALIMENTACAO, "#F97316");

        MovementEntity income = movementService.createIncome(
                "Salário mensal",
                new BigDecimal("5000.00"),
                LocalDate.now().withDayOfMonth(5),
                salary,
                null,
                MovementStatus.PAGO);

        MovementEntity expense = movementService.createExpense(
                "Compras",
                new BigDecimal("250.50"),
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                food,
                "feira",
                MovementStatus.PENDENTE,
                false);

        assertEquals(MovementType.RECEITA, income.getType());
        assertEquals(MovementType.DESPESA, expense.getType());
        assertEquals(1, movementService.listByType(MovementType.RECEITA).size());
        assertEquals(1, movementService.search(MovementType.DESPESA, "com", null, null).size());

        movementService.delete(expense.getId());
        assertTrue(movementService.listByType(MovementType.DESPESA).isEmpty());
        assertFalse(movementService.listByType(MovementType.RECEITA).isEmpty());
    }
}
