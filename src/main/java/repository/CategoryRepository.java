package repository;

import model.CategoryEntity;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    CategoryEntity save(CategoryEntity category);

    Optional<CategoryEntity> findById(Long id);

    List<CategoryEntity> findAll();

    List<CategoryEntity> findByNameContainingIgnoreCase(String query);

    void deleteById(Long id);
}