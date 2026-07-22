package repository;

import config.JpaSupport;
import jakarta.persistence.TypedQuery;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JpaMovementRepository implements MovementRepository {

    @Override
    public MovementEntity save(MovementEntity movement) {
        return JpaSupport.inTransaction(em -> {
            if (movement.getCategory() != null && movement.getCategory().getId() != null) {
                movement.setCategory(em.getReference(model.CategoryEntity.class, movement.getCategory().getId()));
            } else {
                movement.setCategory(null);
            }
            if (movement.getUser() != null && movement.getUser().getId() != null) {
                movement.setUser(em.getReference(model.UserEntity.class, movement.getUser().getId()));
            }
            return em.merge(movement);
        });
    }

    @Override
    public Optional<MovementEntity> findById(Long id) {
        return JpaSupport.readOnly(em -> Optional.ofNullable(em.find(MovementEntity.class, id)));
    }

    @Override
    public void deleteById(Long id) {
        JpaSupport.inTransaction(em -> {
            MovementEntity movement = em.find(MovementEntity.class, id);
            if (movement != null) {
                em.remove(movement);
            }
        });
    }

    @Override
    public List<MovementEntity> findAll() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select m from MovementEntity m order by m.movementDate desc, m.id desc",
                        MovementEntity.class)
                .getResultList());
    }

    @Override
    public List<MovementEntity> findByType(MovementType type) {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select m from MovementEntity m where m.type = :type order by m.movementDate desc, m.id desc",
                        MovementEntity.class)
                .setParameter("type", type)
                .getResultList());
    }

    @Override
    public List<MovementEntity> findByTypeAndPeriod(MovementType type, LocalDate start, LocalDate end) {
        return JpaSupport.readOnly(em -> em.createQuery(
                        """
                        select m from MovementEntity m
                        where m.type = :type
                          and m.movementDate >= :start
                          and m.movementDate <= :end
                        order by m.movementDate desc, m.id desc
                        """,
                        MovementEntity.class)
                .setParameter("type", type)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList());
    }

    @Override
    public List<MovementEntity> findByPeriod(LocalDate start, LocalDate end) {
        return JpaSupport.readOnly(em -> em.createQuery(
                        """
                        select m from MovementEntity m
                        where m.movementDate >= :start
                          and m.movementDate <= :end
                        order by m.movementDate desc, m.id desc
                        """,
                        MovementEntity.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList());
    }

    @Override
    public List<MovementEntity> findRecent(int limit) {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select m from MovementEntity m order by m.movementDate desc, m.id desc",
                        MovementEntity.class)
                .setMaxResults(Math.max(0, limit))
                .getResultList());
    }

    @Override
    public List<MovementEntity> search(MovementType typeOrNull, String query, LocalDate startOrNull, LocalDate endOrNull) {
        return JpaSupport.readOnly(em -> {
            StringBuilder jpql = new StringBuilder(
                    "select m from MovementEntity m where 1 = 1");
            Map<String, Object> params = new HashMap<>();

            if (typeOrNull != null) {
                jpql.append(" and m.type = :type");
                params.put("type", typeOrNull);
            }
            if (query != null && !query.isBlank()) {
                jpql.append(" and lower(m.description) like lower(:query)");
                params.put("query", "%" + query.trim() + "%");
            }
            if (startOrNull != null) {
                jpql.append(" and m.movementDate >= :start");
                params.put("start", startOrNull);
            }
            if (endOrNull != null) {
                jpql.append(" and m.movementDate <= :end");
                params.put("end", endOrNull);
            }

            jpql.append(" order by m.movementDate desc, m.id desc");

            TypedQuery<MovementEntity> typedQuery = em.createQuery(jpql.toString(), MovementEntity.class);
            params.forEach(typedQuery::setParameter);
            return typedQuery.getResultList();
        });
    }

    @Override
    public BigDecimal sumByTypeAndPeriodAndStatus(MovementType type, LocalDate start, LocalDate end,
                                                   MovementStatus statusOrNull) {
        return JpaSupport.readOnly(em -> {
            StringBuilder jpql = new StringBuilder(
                    """
                    select coalesce(sum(m.amount), 0)
                    from MovementEntity m
                    where m.type = :type
                      and m.movementDate >= :start
                      and m.movementDate <= :end
                    """);
            if (statusOrNull != null) {
                jpql.append(" and m.status = :status");
            }

            var query = em.createQuery(jpql.toString(), BigDecimal.class)
                    .setParameter("type", type)
                    .setParameter("start", start)
                    .setParameter("end", end);
            if (statusOrNull != null) {
                query.setParameter("status", statusOrNull);
            }
            BigDecimal result = query.getSingleResult();
            return result == null ? BigDecimal.ZERO : result;
        });
    }

    @Override
    public BigDecimal sumByType(MovementType type, MovementStatus statusOrNull) {
        return JpaSupport.readOnly(em -> {
            StringBuilder jpql = new StringBuilder(
                    "select coalesce(sum(m.amount), 0) from MovementEntity m where m.type = :type");
            if (statusOrNull != null) {
                jpql.append(" and m.status = :status");
            }

            var query = em.createQuery(jpql.toString(), BigDecimal.class)
                    .setParameter("type", type);
            if (statusOrNull != null) {
                query.setParameter("status", statusOrNull);
            }
            BigDecimal result = query.getSingleResult();
            return result == null ? BigDecimal.ZERO : result;
        });
    }

    @Override
    public BigDecimal sumByCreditCardId(Long cardId) {
        return JpaSupport.readOnly(em -> {
            BigDecimal result = em.createQuery(
                            """
                            select coalesce(sum(m.amount), 0)
                            from MovementEntity m
                            where m.creditCardId = :cardId
                              and m.type = :type
                            """,
                            BigDecimal.class)
                    .setParameter("cardId", cardId)
                    .setParameter("type", MovementType.DESPESA)
                    .getSingleResult();
            return result == null ? BigDecimal.ZERO : result;
        });
    }

    @Override
    public BigDecimal sumPendingByCreditCardId(Long cardId) {
        return JpaSupport.readOnly(em -> {
            BigDecimal result = em.createQuery(
                            """
                            select coalesce(sum(m.amount), 0)
                            from MovementEntity m
                            where m.creditCardId = :cardId
                              and m.type = :type
                              and m.status = :status
                            """,
                            BigDecimal.class)
                    .setParameter("cardId", cardId)
                    .setParameter("type", MovementType.DESPESA)
                    .setParameter("status", MovementStatus.PENDENTE)
                    .getSingleResult();
            return result == null ? BigDecimal.ZERO : result;
        });
    }

    @Override
    public List<MovementEntity> findByInstallmentPlanId(Long installmentPlanId) {
        return JpaSupport.readOnly(em -> em.createQuery(
                        """
                        select m from MovementEntity m
                        where m.installmentPlanId = :planId
                        order by m.installmentNumber asc, m.id asc
                        """,
                        MovementEntity.class)
                .setParameter("planId", installmentPlanId)
                .getResultList());
    }

    @Override
    public void deleteByInstallmentPlanId(Long installmentPlanId) {
        JpaSupport.inTransaction(em -> {
            List<MovementEntity> movements = em.createQuery(
                            "select m from MovementEntity m where m.installmentPlanId = :planId",
                            MovementEntity.class)
                    .setParameter("planId", installmentPlanId)
                    .getResultList();
            for (MovementEntity movement : movements) {
                em.remove(movement);
            }
        });
    }
}
