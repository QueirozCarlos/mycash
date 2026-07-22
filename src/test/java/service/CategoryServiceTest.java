package service;

import config.ApplicationBootstrap;
import config.DatabaseBootstrap;
import model.CategoryEntity;
import model.CategoryType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CategoryServiceTest {

    @Test
    void shouldCreateSearchUpdateAndDeleteCategory(@TempDir Path tempDir) {
        ApplicationBootstrap.initialize(tempDir);
        DatabaseBootstrap.initialize(tempDir);

        CategoryService categoryService = new CategoryService();

        CategoryEntity created = categoryService.create("Mercado", CategoryType.ALIMENTACAO, "#22AA88");
        assertNotNull(created.getId());
        assertEquals("Mercado", created.getName());
        assertEquals(1, categoryService.list("").size());
        assertFalse(categoryService.list("transporte").stream().findAny().isPresent());
        assertTrue(categoryService.list("mer").stream().findAny().isPresent());

        CategoryEntity updated = categoryService.update(created.getId(), "Supermercado", CategoryType.ALIMENTACAO, "#112233", true);
        assertEquals("Supermercado", updated.getName());
        assertEquals("#112233", updated.getColorHex());

        categoryService.delete(created.getId());
        assertTrue(categoryService.list("").isEmpty());

        DatabaseBootstrap.shutdown();
    }
}