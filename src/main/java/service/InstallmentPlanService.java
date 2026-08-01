package service;

import exception.ApplicationException;
import exception.InstallmentPlanNotFoundException;
import model.CategoryEntity;
import model.CreditCardEntity;
import model.InstallmentPlanEntity;
import model.MovementEntity;
import model.MovementStatus;
import model.MovementType;
import repository.InstallmentPlanRepository;
import repository.JpaInstallmentPlanRepository;
import repository.JpaMovementRepository;
import repository.MovementRepository;
import validation.ValidationUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class InstallmentPlanService {

    private final InstallmentPlanRepository installmentPlanRepository;
    private final MovementRepository movementRepository;
    private final MovementService movementService;
    private final UserService userService;
    private final CreditCardService creditCardService;

    public InstallmentPlanService() {
        this(new JpaInstallmentPlanRepository(), new JpaMovementRepository(), new MovementService(), new UserService(), new CreditCardService());
    }

    InstallmentPlanService(InstallmentPlanRepository installmentPlanRepository,
                           MovementRepository movementRepository,
                           MovementService movementService,
                           UserService userService,
                           CreditCardService creditCardService) {
        this.installmentPlanRepository = installmentPlanRepository;
        this.movementRepository = movementRepository;
        this.movementService = movementService;
        this.userService = userService;
        this.creditCardService = creditCardService;
    }

    public List<InstallmentPlanEntity> list() {
        return installmentPlanRepository.findAll();
    }

    public List<InstallmentPlanEntity> listByCreditCard(Long creditCardId) {
        return installmentPlanRepository.findByCreditCardId(creditCardId);
    }

    public InstallmentPlanEntity findById(Long id) {
        return installmentPlanRepository.findById(id)
                .orElseThrow(() -> new InstallmentPlanNotFoundException(id));
    }

    public InstallmentPlanEntity create(CreditCardEntity creditCard, String description, BigDecimal totalAmount,
                                        int installmentCount, LocalDate startDate, CategoryEntity category) {
        ValidationUtils.requireNonBlank(description, "Descrição");
        ValidationUtils.requirePositive(totalAmount, "Valor total");
        if (installmentCount <= 0) {
            throw new IllegalArgumentException("Quantidade de parcelas deve ser maior que zero.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula.");
        }

        // --- VALIDAÇÃO DE LIMITE DO CARTÃO ---
        if (creditCard != null && creditCard.getId() != null) {
            var summary = creditCardService.summarize(creditCard.getId());
            if (totalAmount.compareTo(summary.availableLimit()) > 0) {
                throw new ApplicationException("Limite insuficiente no cartão de crédito! Limite disponível: R$ "
                        + String.format("%.2f", summary.availableLimit()));
            }
        }

        userService.ensureDefaultUser();

        BigDecimal installmentAmount = totalAmount
                .divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.DOWN);

        BigDecimal distributed = installmentAmount.multiply(BigDecimal.valueOf(installmentCount));

        BigDecimal difference = totalAmount.subtract(distributed);

        InstallmentPlanEntity plan = new InstallmentPlanEntity(
                creditCard,
                description.trim(),
                totalAmount,
                installmentCount,
                installmentAmount,
                startDate,
                category);
        plan = installmentPlanRepository.save(plan);

        Long cardId = creditCard == null ? null : creditCard.getId();

        for (int number = 1; number <= installmentCount; number++) {

            BigDecimal currentInstallmentAmount = installmentAmount;

            if (number == installmentCount) {
                currentInstallmentAmount = installmentAmount.add(difference);
            }

            LocalDate installmentDate = startDate.plusMonths(number - 1L);
            String installmentDescription = description.trim() + " (" + number + "/" + installmentCount + ")";
            movementService.create(
                    MovementType.DESPESA,
                    installmentDescription,
                    currentInstallmentAmount,
                    installmentDate,
                    installmentDate,
                    category,
                    null,
                    MovementStatus.PENDENTE,
                    false,
                    null,
                    cardId,
                    plan.getId(),
                    number);
        }

        return plan;
    }

    public InstallmentPlanEntity markNextPaid(Long planId) {
        InstallmentPlanEntity plan = findById(planId);
        if (plan.getPaidInstallments() >= plan.getInstallmentCount()) {
            throw new ApplicationException("Todas as parcelas deste plano já foram pagas.");
        }

        int nextNumber = plan.getPaidInstallments() + 1;
        List<MovementEntity> movements = movementRepository.findByInstallmentPlanId(planId);
        MovementEntity target = movements.stream()
                .filter(m -> nextNumber == (m.getInstallmentNumber() == null ? -1 : m.getInstallmentNumber()))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(
                        "Movimentação da parcela " + nextNumber + " não encontrada para o plano " + planId + "."));

        target.setStatus(MovementStatus.PAGO);
        movementRepository.save(target);

        plan.setPaidInstallments(nextNumber);
        return installmentPlanRepository.save(plan);
    }

    public void delete(Long id) {
        if (installmentPlanRepository.findById(id).isEmpty()) {
            throw new InstallmentPlanNotFoundException(id);
        }
        movementRepository.deleteByInstallmentPlanId(id);
        installmentPlanRepository.deleteById(id);
    }
}
