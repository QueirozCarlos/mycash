package repository;

import config.JpaSupport;
import model.RecurringAccountEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JpaRecurringAccountRepository implements RecurringAccountRepository {

    @Override
    public RecurringAccountEntity save(RecurringAccountEntity account) {
        return JpaSupport.inTransaction(em -> {
            if (account.getCategory() != null && account.getCategory().getId() != null) {
                account.setCategory(em.getReference(model.CategoryEntity.class, account.getCategory().getId()));
            } else {
                account.setCategory(null);
            }
            return em.merge(account);
        });
    }

    @Override
    public Optional<RecurringAccountEntity> findById(Long id) {
        return JpaSupport.readOnly(em -> Optional.ofNullable(em.find(RecurringAccountEntity.class, id)));
    }

    @Override
    public List<RecurringAccountEntity> findAll() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select r from RecurringAccountEntity r order by r.nextOccurrence asc, r.id asc",
                        RecurringAccountEntity.class)
                .getResultList());
    }

    @Override
    public void deleteById(Long id) {
        JpaSupport.inTransaction(em -> {
            RecurringAccountEntity account = em.find(RecurringAccountEntity.class, id);
            if (account != null) {
                em.remove(account);
            }
        });
    }

    @Override
    public List<RecurringAccountEntity> findActiveDueOnOrBefore(LocalDate date) {
        return JpaSupport.readOnly(em -> {
            List<RecurringAccountEntity> active = em.createQuery(
                            "select r from RecurringAccountEntity r where r.active = true order by r.nextOccurrence asc, r.id asc",
                            RecurringAccountEntity.class)
                    .getResultList();
            return active.stream()
                    .filter(account -> account.getNextOccurrence() != null
                            && !account.getNextOccurrence().isAfter(date))
                    .toList();
        });
    }
}
