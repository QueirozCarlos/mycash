package repository;

import config.JpaSupport;
import model.CategoryEntity;

import java.util.List;
import java.util.Optional;

public class JpaCategoryRepository implements CategoryRepository {

    @Override
    public CategoryEntity save(CategoryEntity category) {
        return JpaSupport.inTransaction(em -> {
            return em.merge(category);
        });
    }

    @Override
    public Optional<CategoryEntity> findById(Long id) {
        return JpaSupport.readOnly(em -> Optional.ofNullable(em.find(CategoryEntity.class, id)));
    }

    @Override
    public List<CategoryEntity> findAll() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select c from CategoryEntity c order by c.name", CategoryEntity.class)
                .getResultList());
    }

    @Override
    public List<CategoryEntity> findByNameContainingIgnoreCase(String query) {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select c from CategoryEntity c where lower(c.name) like lower(:query) order by c.name",
                        CategoryEntity.class)
                .setParameter("query", "%" + query + "%")
                .getResultList());
    }

    @Override
    public void deleteById(Long id) {
        JpaSupport.inTransaction(em -> {
            CategoryEntity category = em.find(CategoryEntity.class, id);
            if (category != null) {
                em.remove(category);
            }
        });
    }
}
