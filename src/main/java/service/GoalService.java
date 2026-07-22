package service;

import exception.GoalNotFoundException;
import model.GoalEntity;
import repository.GoalRepository;
import repository.JpaGoalRepository;
import validation.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService() {
        this(new JpaGoalRepository());
    }

    GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public List<GoalEntity> list() {
        return goalRepository.findAll();
    }

    public GoalEntity findById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new GoalNotFoundException(id));
    }

    public GoalEntity create(String name, String description, BigDecimal targetAmount,
                             BigDecimal currentAmount, LocalDate deadline) {
        ValidationUtils.requireNonBlank(name, "Nome da meta");
        ValidationUtils.requirePositive(targetAmount, "Valor alvo");
        if (currentAmount != null && currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor atual não pode ser negativo.");
        }

        GoalEntity goal = new GoalEntity(
                name.trim(),
                description,
                targetAmount,
                currentAmount == null ? BigDecimal.ZERO : currentAmount,
                deadline);
        return goalRepository.save(goal);
    }

    public GoalEntity update(Long id, String name, String description, BigDecimal targetAmount,
                             BigDecimal currentAmount, LocalDate deadline, boolean active) {
        ValidationUtils.requireNonBlank(name, "Nome da meta");
        ValidationUtils.requirePositive(targetAmount, "Valor alvo");
        if (currentAmount != null && currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor atual não pode ser negativo.");
        }

        GoalEntity goal = findById(id);
        goal.setName(name.trim());
        goal.setDescription(description);
        goal.setTargetAmount(targetAmount);
        goal.setCurrentAmount(currentAmount == null ? BigDecimal.ZERO : currentAmount);
        goal.setDeadline(deadline);
        goal.setActive(active);
        return goalRepository.save(goal);
    }

    public GoalEntity addProgress(Long id, BigDecimal amount) {
        ValidationUtils.requirePositive(amount, "Valor do progresso");
        GoalEntity goal = findById(id);
        BigDecimal current = goal.getCurrentAmount() == null ? BigDecimal.ZERO : goal.getCurrentAmount();
        goal.setCurrentAmount(current.add(amount));
        return goalRepository.save(goal);
    }

    public void delete(Long id) {
        if (goalRepository.findById(id).isEmpty()) {
            throw new GoalNotFoundException(id);
        }
        goalRepository.deleteById(id);
    }
}
