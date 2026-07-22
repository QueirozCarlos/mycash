package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exception.ApplicationException;
import model.CategoryEntity;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataExchangeService {

    private final ObjectMapper objectMapper;
    private final CategoryService categoryService;
    private final MovementService movementService;

    public DataExchangeService() {
        this(new CategoryService(), new MovementService());
    }

    DataExchangeService(CategoryService categoryService, MovementService movementService) {
        this.categoryService = categoryService;
        this.movementService = movementService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Path exportJson(Path target) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("exportedAt", LocalDate.now().toString());
            payload.put("categories", categoryService.list("").stream().map(this::toCategoryMap).toList());
            payload.put("movements", movementService.list().stream().map(this::toMovementMap).toList());
            objectMapper.writeValue(target.toFile(), payload);
            return target;
        } catch (IOException exception) {
            throw new ApplicationException("Falha ao exportar JSON.", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public int importJson(Path source) {
        try {
            Map<String, Object> payload = objectMapper.readValue(source.toFile(), Map.class);
            Object rawMovements = payload.get("movements");
            if (!(rawMovements instanceof List<?> movements)) {
                return 0;
            }

            Map<String, CategoryEntity> categoriesByName = new LinkedHashMap<>();
            for (CategoryEntity category : categoryService.list("")) {
                categoriesByName.put(category.getName().toLowerCase(), category);
            }

            int imported = 0;
            for (Object item : movements) {
                if (!(item instanceof Map<?, ?> row)) {
                    continue;
                }
                MovementType type = MovementType.valueOf(String.valueOf(row.get("type")));
                String description = String.valueOf(row.get("description"));
                BigDecimal amount = new BigDecimal(String.valueOf(row.get("amount")));
                LocalDate date = LocalDate.parse(String.valueOf(row.get("movementDate")));
                MovementStatus status = MovementStatus.valueOf(String.valueOf(row.get("status")));
                String categoryName = row.get("category") == null ? null : String.valueOf(row.get("category"));
                CategoryEntity category = categoryName == null || categoryName.isBlank()
                        ? null
                        : categoriesByName.get(categoryName.toLowerCase());
                String notes = row.get("notes") == null ? null : String.valueOf(row.get("notes"));

                if (type == MovementType.RECEITA) {
                    movementService.createIncome(description, amount, date, category, notes, status);
                } else {
                    LocalDate dueDate = row.get("dueDate") == null || String.valueOf(row.get("dueDate")).isBlank()
                            ? null
                            : LocalDate.parse(String.valueOf(row.get("dueDate")));
                    movementService.createExpense(description, amount, date, dueDate, category, notes, status, false);
                }
                imported++;
            }
            return imported;
        } catch (IOException | RuntimeException exception) {
            throw new ApplicationException("Falha ao importar JSON.", exception);
        }
    }

    private Map<String, Object> toCategoryMap(CategoryEntity category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", category.getName());
        map.put("type", category.getType().name());
        map.put("colorHex", category.getColorHex());
        map.put("active", category.isActive());
        return map;
    }

    private Map<String, Object> toMovementMap(MovementEntity movement) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", movement.getType().name());
        map.put("description", movement.getDescription());
        map.put("amount", movement.getAmount());
        map.put("movementDate", movement.getMovementDate());
        map.put("dueDate", movement.getDueDate());
        map.put("status", movement.getStatus().name());
        map.put("category", movement.getCategoryName());
        map.put("notes", movement.getNotes());
        return map;
    }
}
