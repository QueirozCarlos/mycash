package service;

import exception.MovementNotFoundException;
import model.CategoryEntity;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;
import model.UserEntity;
import repository.JpaMovementRepository;
import repository.MovementRepository;
import validation.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MovementService {

    private final MovementRepository movementRepository;
    private final UserService userService;

    public MovementService() {
        this(new JpaMovementRepository(), new UserService());
    }

    MovementService(MovementRepository movementRepository, UserService userService) {
        this.movementRepository = movementRepository;
        this.userService = userService;
    }

    public List<MovementEntity> list() {
        return movementRepository.findAll();
    }

    public List<MovementEntity> listByType(MovementType type) {
        return movementRepository.findByType(type);
    }

    public List<MovementEntity> search(MovementType typeOrNull, String query, LocalDate startOrNull, LocalDate endOrNull) {
        return movementRepository.search(typeOrNull, query, startOrNull, endOrNull);
    }

    public MovementEntity findById(Long id) {
        return movementRepository.findById(id)
                .orElseThrow(() -> new MovementNotFoundException(id));
    }

    public MovementEntity createIncome(String description, BigDecimal amount, LocalDate movementDate,
                                       CategoryEntity category, String notes, MovementStatus status) {
        return create(MovementType.RECEITA, description, amount, movementDate, null, category, notes,
                status == null ? MovementStatus.PAGO : status, false, null, null, null, null);
    }

    public MovementEntity createExpense(String description, BigDecimal amount, LocalDate movementDate,
                                        LocalDate dueDate, CategoryEntity category, String notes,
                                        MovementStatus status, boolean recurring) {
        return create(MovementType.DESPESA, description, amount, movementDate, dueDate, category, notes,
                status == null ? MovementStatus.PENDENTE : status, recurring, null, null, null, null);
    }

    public MovementEntity create(MovementType type, String description, BigDecimal amount, LocalDate movementDate,
                                 LocalDate dueDate, CategoryEntity category, String notes, MovementStatus status,
                                 boolean recurring, Long recurringAccountId, Long creditCardId,
                                 Long installmentPlanId, Integer installmentNumber) {
        validate(description, amount, movementDate, type, status);

        UserEntity user = userService.ensureDefaultUser();
        MovementEntity movement = new MovementEntity(
                type,
                description.trim(),
                amount,
                movementDate,
                status,
                category,
                user,
                notes);
        movement.setDueDate(dueDate);
        movement.setRecurring(recurring);
        movement.setRecurringAccountId(recurringAccountId);
        movement.setCreditCardId(creditCardId);
        movement.setInstallmentPlanId(installmentPlanId);
        movement.setInstallmentNumber(installmentNumber);
        return movementRepository.save(movement);
    }

    public MovementEntity update(Long id, String description, BigDecimal amount, LocalDate movementDate,
                                 LocalDate dueDate, CategoryEntity category, String notes, MovementStatus status,
                                 boolean recurring) {
        MovementEntity movement = findById(id);
        validate(description, amount, movementDate, movement.getType(), status);

        movement.setDescription(description.trim());
        movement.setAmount(amount);
        movement.setMovementDate(movementDate);
        movement.setDueDate(dueDate);
        movement.setCategory(category);
        movement.setNotes(notes);
        movement.setStatus(status);
        movement.setRecurring(recurring);
        return movementRepository.save(movement);
    }

    public void delete(Long id) {
        if (movementRepository.findById(id).isEmpty()) {
            throw new MovementNotFoundException(id);
        }
        movementRepository.deleteById(id);
    }

    private void validate(String description, BigDecimal amount, LocalDate movementDate,
                          MovementType type, MovementStatus status) {
        ValidationUtils.requireNonBlank(description, "Descrição");
        ValidationUtils.requirePositive(amount, "Valor");
        if (movementDate == null) {
            throw new IllegalArgumentException("Data do movimento não pode ser nula.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Tipo do movimento não pode ser nulo.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status do movimento não pode ser nulo.");
        }
    }
}
