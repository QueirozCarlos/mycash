package repository;

import config.JpaSupport;
import model.InstallmentPlanEntity;

import java.util.List;
import java.util.Optional;

public class JpaInstallmentPlanRepository implements InstallmentPlanRepository {

    @Override
    public InstallmentPlanEntity save(InstallmentPlanEntity plan) {
        return JpaSupport.inTransaction(em -> {
            if (plan.getCreditCard() != null && plan.getCreditCard().getId() != null) {
                plan.setCreditCard(em.getReference(model.CreditCardEntity.class, plan.getCreditCard().getId()));
            } else {
                plan.setCreditCard(null);
            }
            if (plan.getCategory() != null && plan.getCategory().getId() != null) {
                plan.setCategory(em.getReference(model.CategoryEntity.class, plan.getCategory().getId()));
            } else {
                plan.setCategory(null);
            }
            return em.merge(plan);
        });
    }

    @Override
    public Optional<InstallmentPlanEntity> findById(Long id) {
        return JpaSupport.readOnly(em -> Optional.ofNullable(em.find(InstallmentPlanEntity.class, id)));
    }

    @Override
    public List<InstallmentPlanEntity> findAll() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select p from InstallmentPlanEntity p order by p.startDate desc, p.id desc",
                        InstallmentPlanEntity.class)
                .getResultList());
    }

    @Override
    public List<InstallmentPlanEntity> findByCreditCardId(Long creditCardId) {
        return JpaSupport.readOnly(em -> em.createQuery(
                        """
                        select p from InstallmentPlanEntity p
                        where p.creditCard.id = :cardId
                        order by p.startDate desc, p.id desc
                        """,
                        InstallmentPlanEntity.class)
                .setParameter("cardId", creditCardId)
                .getResultList());
    }

    @Override
    public void deleteById(Long id) {
        JpaSupport.inTransaction(em -> {
            InstallmentPlanEntity plan = em.find(InstallmentPlanEntity.class, id);
            if (plan != null) {
                em.remove(plan);
            }
        });
    }
}
