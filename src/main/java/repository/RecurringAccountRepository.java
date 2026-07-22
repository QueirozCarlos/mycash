package repository;

import model.RecurringAccountEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringAccountRepository {

    RecurringAccountEntity save(RecurringAccountEntity account);

    Optional<RecurringAccountEntity> findById(Long id);

    List<RecurringAccountEntity> findAll();

    void deleteById(Long id);

    List<RecurringAccountEntity> findActiveDueOnOrBefore(LocalDate date);
}
