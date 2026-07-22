package service;

import dto.CreditCardSummaryDto;
import exception.CreditCardNotFoundException;
import model.CreditCardEntity;
import repository.CreditCardRepository;
import repository.JpaCreditCardRepository;
import repository.JpaMovementRepository;
import repository.MovementRepository;
import validation.ValidationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final MovementRepository movementRepository;

    public CreditCardService() {
        this(new JpaCreditCardRepository(), new JpaMovementRepository());
    }

    CreditCardService(CreditCardRepository creditCardRepository, MovementRepository movementRepository) {
        this.creditCardRepository = creditCardRepository;
        this.movementRepository = movementRepository;
    }

    public List<CreditCardEntity> list() {
        return creditCardRepository.findAll();
    }

    public CreditCardEntity findById(Long id) {
        return creditCardRepository.findById(id)
                .orElseThrow(() -> new CreditCardNotFoundException(id));
    }

    public CreditCardEntity create(String name, BigDecimal creditLimit, int closingDay, int dueDay) {
        validate(name, creditLimit, closingDay, dueDay);
        return creditCardRepository.save(new CreditCardEntity(name.trim(), creditLimit, closingDay, dueDay));
    }

    public CreditCardEntity update(Long id, String name, BigDecimal creditLimit, int closingDay, int dueDay,
                                   boolean active) {
        validate(name, creditLimit, closingDay, dueDay);
        CreditCardEntity card = findById(id);
        card.setName(name.trim());
        card.setCreditLimit(creditLimit);
        card.setClosingDay(closingDay);
        card.setDueDay(dueDay);
        card.setActive(active);
        return creditCardRepository.save(card);
    }

    public void delete(Long id) {
        if (creditCardRepository.findById(id).isEmpty()) {
            throw new CreditCardNotFoundException(id);
        }
        creditCardRepository.deleteById(id);
    }

    public CreditCardSummaryDto summarize(CreditCardEntity card) {
        BigDecimal used = movementRepository.sumByCreditCardId(card.getId());
        BigDecimal available = card.getCreditLimit().subtract(used);
        BigDecimal invoiceEstimate = movementRepository.sumPendingByCreditCardId(card.getId());
        return new CreditCardSummaryDto(card, used, available, invoiceEstimate);
    }

    public CreditCardSummaryDto summarize(Long cardId) {
        return summarize(findById(cardId));
    }

    public List<CreditCardSummaryDto> listSummaries() {
        List<CreditCardSummaryDto> summaries = new ArrayList<>();
        for (CreditCardEntity card : creditCardRepository.findAll()) {
            summaries.add(summarize(card));
        }
        return summaries;
    }

    private void validate(String name, BigDecimal creditLimit, int closingDay, int dueDay) {
        ValidationUtils.requireNonBlank(name, "Nome do cartão");
        ValidationUtils.requirePositive(creditLimit, "Limite");
        ValidationUtils.requireDayOfMonth(closingDay);
        ValidationUtils.requireDayOfMonth(dueDay);
    }
}
