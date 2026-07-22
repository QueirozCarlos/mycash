package service;

import exception.RecurringAccountNotFoundException;
import model.CategoryEntity;
import model.MovementStatus;
import model.MovementType;
import model.RecurrenceFrequency;
import model.RecurringAccountEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.JpaRecurringAccountRepository;
import repository.RecurringAccountRepository;
import validation.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RecurringAccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecurringAccountService.class);

    private final RecurringAccountRepository recurringAccountRepository;
    private final MovementService movementService;

    public RecurringAccountService() {
        this(new JpaRecurringAccountRepository(), new MovementService());
    }

    RecurringAccountService(RecurringAccountRepository recurringAccountRepository, MovementService movementService) {
        this.recurringAccountRepository = recurringAccountRepository;
        this.movementService = movementService;
    }

    public List<RecurringAccountEntity> list() {
        return recurringAccountRepository.findAll();
    }

    public RecurringAccountEntity findById(Long id) {
        return recurringAccountRepository.findById(id)
                .orElseThrow(() -> new RecurringAccountNotFoundException(id));
    }

    public RecurringAccountEntity create(String description, MovementType type, BigDecimal amount,
                                         CategoryEntity category, RecurrenceFrequency frequency,
                                         Integer customIntervalDays, LocalDate nextOccurrence, String notes) {
        validate(description, type, amount, frequency, customIntervalDays, nextOccurrence);

        RecurringAccountEntity account = new RecurringAccountEntity(
                description.trim(),
                type,
                amount,
                category,
                frequency,
                customIntervalDays,
                nextOccurrence,
                notes);
        RecurringAccountEntity saved = recurringAccountRepository.save(account);
        int generated = generateDueOccurrences();
        LOGGER.info("Conta recorrente {} criada. Movimentações geradas: {}", saved.getId(), generated);
        return findById(saved.getId());
    }

    public RecurringAccountEntity update(Long id, String description, MovementType type, BigDecimal amount,
                                         CategoryEntity category, RecurrenceFrequency frequency,
                                         Integer customIntervalDays, LocalDate nextOccurrence,
                                         boolean active, String notes) {
        validate(description, type, amount, frequency, customIntervalDays, nextOccurrence);

        RecurringAccountEntity account = findById(id);
        account.setDescription(description.trim());
        account.setType(type);
        account.setAmount(amount);
        account.setCategory(category);
        account.setFrequency(frequency);
        account.setCustomIntervalDays(customIntervalDays);
        account.setNextOccurrence(nextOccurrence);
        account.setActive(active);
        account.setNotes(notes);
        RecurringAccountEntity saved = recurringAccountRepository.save(account);
        generateDueOccurrences();
        return findById(saved.getId());
    }

    public void delete(Long id) {
        if (recurringAccountRepository.findById(id).isEmpty()) {
            throw new RecurringAccountNotFoundException(id);
        }
        recurringAccountRepository.deleteById(id);
    }

    /**
     * Creates movements for every active account due on or before today,
     * then advances {@code nextOccurrence} according to frequency.
     *
     * @return number of movements created
     */
    public int generateDueOccurrences() {
        LocalDate today = LocalDate.now();
        List<RecurringAccountEntity> dueAccounts = recurringAccountRepository.findActiveDueOnOrBefore(today);
        int created = 0;

        for (RecurringAccountEntity due : dueAccounts) {
            // Reload managed snapshot to avoid stale detached state across transactions.
            RecurringAccountEntity account = findById(due.getId());
            while (account.isActive() && !account.getNextOccurrence().isAfter(today)) {
                LocalDate occurrenceDate = account.getNextOccurrence();
                MovementStatus status = account.getType() == MovementType.RECEITA
                        ? MovementStatus.PAGO
                        : MovementStatus.PENDENTE;

                movementService.create(
                        account.getType(),
                        account.getDescription(),
                        account.getAmount(),
                        occurrenceDate,
                        account.getType() == MovementType.DESPESA ? occurrenceDate : null,
                        account.getCategory(),
                        account.getNotes(),
                        status,
                        true,
                        account.getId(),
                        null,
                        null,
                        null);

                account.setNextOccurrence(advance(occurrenceDate, account.getFrequency(), account.getCustomIntervalDays()));
                account = recurringAccountRepository.save(account);
                created++;
            }
        }

        LOGGER.info("Geração de recorrentes: {} movimentação(ões) em {}", created, today);
        return created;
    }

    private LocalDate advance(LocalDate from, RecurrenceFrequency frequency, Integer customIntervalDays) {
        return switch (frequency) {
            case SEMANAL -> from.plusWeeks(1);
            case MENSAL -> from.plusMonths(1);
            case ANUAL -> from.plusYears(1);
            case PERSONALIZADA -> {
                if (customIntervalDays == null || customIntervalDays <= 0) {
                    throw new IllegalArgumentException("Intervalo personalizado deve ser maior que zero.");
                }
                yield from.plusDays(customIntervalDays);
            }
        };
    }

    private void validate(String description, MovementType type, BigDecimal amount,
                          RecurrenceFrequency frequency, Integer customIntervalDays, LocalDate nextOccurrence) {
        ValidationUtils.requireNonBlank(description, "Descrição");
        ValidationUtils.requirePositive(amount, "Valor");
        if (type == null) {
            throw new IllegalArgumentException("Tipo não pode ser nulo.");
        }
        if (frequency == null) {
            throw new IllegalArgumentException("Frequência não pode ser nula.");
        }
        if (nextOccurrence == null) {
            throw new IllegalArgumentException("Próxima ocorrência não pode ser nula.");
        }
        if (frequency == RecurrenceFrequency.PERSONALIZADA
                && (customIntervalDays == null || customIntervalDays <= 0)) {
            throw new IllegalArgumentException("Intervalo personalizado deve ser maior que zero.");
        }
    }
}
