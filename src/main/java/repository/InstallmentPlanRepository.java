package repository;

import model.InstallmentPlanEntity;

import java.util.List;
import java.util.Optional;

public interface InstallmentPlanRepository {

    InstallmentPlanEntity save(InstallmentPlanEntity plan);

    Optional<InstallmentPlanEntity> findById(Long id);

    List<InstallmentPlanEntity> findAll();

    List<InstallmentPlanEntity> findByCreditCardId(Long creditCardId);

    void deleteById(Long id);
}
