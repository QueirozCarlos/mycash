package service;

import exception.CategoryNotFoundException;
import model.CategoryEntity;
import model.CategoryType;
import repository.CategoryRepository;
import repository.JpaCategoryRepository;
import validation.ValidationUtils;

import java.util.List;

public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this(new JpaCategoryRepository());
    }

    CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryEntity> list(String query) {
        if (query == null || query.isBlank()) {
            return categoryRepository.findAll();
        }

        return categoryRepository.findByNameContainingIgnoreCase(query.trim());
    }

    public CategoryEntity create(String name, CategoryType type, String colorHex) {
        validate(name, type);
        CategoryEntity category = new CategoryEntity(name.trim(), type, normalizeColor(colorHex));
        return categoryRepository.save(category);
    }

    public CategoryEntity update(Long id, String name, CategoryType type, String colorHex, boolean active) {
        validate(name, type);

        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        category.setName(name.trim());
        category.setType(type);
        category.setColorHex(normalizeColor(colorHex));
        category.setActive(active);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    private void validate(String name, CategoryType type) {
        ValidationUtils.requireNonBlank(name, "Nome da categoria");
        if (type == null) {
            throw new IllegalArgumentException("Tipo da categoria não pode estar vazio.");
        }
    }

    private String normalizeColor(String colorHex) {
        return colorHex == null || colorHex.isBlank() ? "#5B6CFF" : colorHex.trim();
    }
}