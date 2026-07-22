package repository;

import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovementRepository {

    MovementEntity save(MovementEntity movement);

    Optional<MovementEntity> findById(Long id);

    void deleteById(Long id);

    List<MovementEntity> findAll();

    List<MovementEntity> findByType(MovementType type);

    List<MovementEntity> findByTypeAndPeriod(MovementType type, LocalDate start, LocalDate end);

    List<MovementEntity> findByPeriod(LocalDate start, LocalDate end);

    List<MovementEntity> findRecent(int limit);

    List<MovementEntity> search(MovementType typeOrNull, String query, LocalDate startOrNull, LocalDate endOrNull);

    BigDecimal sumByTypeAndPeriodAndStatus(MovementType type, LocalDate start, LocalDate end, MovementStatus statusOrNull);

    BigDecimal sumByType(MovementType type, MovementStatus statusOrNull);

    BigDecimal sumByCreditCardId(Long cardId);

    List<MovementEntity> findByInstallmentPlanId(Long installmentPlanId);

    void deleteByInstallmentPlanId(Long installmentPlanId);

    BigDecimal sumPendingByCreditCardId(Long cardId);
}
