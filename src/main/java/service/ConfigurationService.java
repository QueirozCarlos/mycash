package service;

import config.JpaSupport;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Simple key-value configuration stored in the {@code configuracao} table.
 */
public class ConfigurationService {

    public Optional<String> get(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }

        return JpaSupport.readOnly(em -> {
            @SuppressWarnings("unchecked")
            List<Object> results = em.createNativeQuery(
                            "select valor from configuracao where chave = ?1")
                    .setParameter(1, key)
                    .getResultList();
            if (results.isEmpty() || results.getFirst() == null) {
                return Optional.empty();
            }
            return Optional.of(String.valueOf(results.getFirst()));
        });
    }

    public void set(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Chave de configuração não pode estar vazia.");
        }
        if (value == null) {
            throw new IllegalArgumentException("Valor de configuração não pode ser nulo.");
        }

        String updatedAt = LocalDateTime.now().toString();
        JpaSupport.inTransaction(em -> {
            int updated = em.createNativeQuery(
                            "update configuracao set valor = ?1, updated_at = ?2 where chave = ?3")
                    .setParameter(1, value)
                    .setParameter(2, updatedAt)
                    .setParameter(3, key)
                    .executeUpdate();

            if (updated == 0) {
                em.createNativeQuery(
                                "insert into configuracao (chave, valor, updated_at) values (?1, ?2, ?3)")
                        .setParameter(1, key)
                        .setParameter(2, value)
                        .setParameter(3, updatedAt)
                        .executeUpdate();
            }
        });
    }

    public Map<String, String> findAll() {
        return JpaSupport.readOnly(em -> {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                            "select chave, valor from configuracao order by chave")
                    .getResultList();
            Map<String, String> map = new LinkedHashMap<>();
            for (Object[] row : rows) {
                map.put(String.valueOf(row[0]), row[1] == null ? null : String.valueOf(row[1]));
            }
            return map;
        });
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        JpaSupport.inTransaction(em -> {
            em.createNativeQuery(
                            "delete from configuracao where chave = ?1")
                    .setParameter(1, key)
                    .executeUpdate();
        });
    }
}
