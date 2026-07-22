package repository;

import config.JpaSupport;
import model.CreditCardEntity;

import java.util.List;
import java.util.Optional;

public class JpaCreditCardRepository implements CreditCardRepository {

    @Override
    public CreditCardEntity save(CreditCardEntity card) {
        return JpaSupport.inTransaction(em -> {
            return em.merge(card);
        });
    }

    @Override
    public Optional<CreditCardEntity> findById(Long id) {
        return JpaSupport.readOnly(em -> Optional.ofNullable(em.find(CreditCardEntity.class, id)));
    }

    @Override
    public List<CreditCardEntity> findAll() {
        return JpaSupport.readOnly(em -> em.createQuery(
                        "select c from CreditCardEntity c order by c.name asc",
                        CreditCardEntity.class)
                .getResultList());
    }

    @Override
    public void deleteById(Long id) {
        JpaSupport.inTransaction(em -> {
            CreditCardEntity card = em.find(CreditCardEntity.class, id);
            if (card != null) {
                em.remove(card);
            }
        });
    }
}
