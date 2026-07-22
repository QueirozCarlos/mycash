package repository;

import model.CreditCardEntity;

import java.util.List;
import java.util.Optional;

public interface CreditCardRepository {

    CreditCardEntity save(CreditCardEntity card);

    Optional<CreditCardEntity> findById(Long id);

    List<CreditCardEntity> findAll();

    void deleteById(Long id);
}
