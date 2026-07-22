package dto;

import model.CreditCardEntity;

import java.math.BigDecimal;

public record CreditCardSummaryDto(
        CreditCardEntity card,
        BigDecimal usedAmount,
        BigDecimal availableLimit,
        BigDecimal currentInvoiceEstimate
) {
}
