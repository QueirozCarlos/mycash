package config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Consumer;
import java.util.function.Function;

public final class JpaSupport {

    private JpaSupport() {
    }

    public static <T> T inTransaction(Function<EntityManager, T> work) {
        EntityManager entityManager = DatabaseBootstrap.entityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            T result = work.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            entityManager.close();
        }
    }

    public static void inTransaction(Consumer<EntityManager> work) {
        inTransaction(entityManager -> {
            work.accept(entityManager);
            return null;
        });
    }

    public static <T> T readOnly(Function<EntityManager, T> work) {
        EntityManager entityManager = DatabaseBootstrap.entityManagerFactory().createEntityManager();
        try {
            return work.apply(entityManager);
        } finally {
            entityManager.close();
        }
    }
}
