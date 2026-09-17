package com.github.wallet.infrastructure.persistence.dto;

import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;
import com.github.wallet.domain.model.Transaction;
import com.github.wallet.domain.model.TransactionId;
import com.github.wallet.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JSON-serializable DTO for Transaction persistence.
 * Maps between domain Transaction record and JSON representation.
 */
public record TransactionJson(
        String id,
        String sourceAccountId,
        String targetAccountId,
        String amount,
        String currency,
        String type,
        String timestamp,
        String description
) {

    /**
     * Convert domain Transaction to JSON DTO.
     */
    public static TransactionJson fromDomain(final Transaction transaction) {
        return new TransactionJson(
                transaction.id().value(),
                transaction.sourceAccountId().value(),
                transaction.targetAccountId() != null ? transaction.targetAccountId().value() : null,
                transaction.amount().amount().toPlainString(),
                transaction.amount().currency().name(),
                transaction.type().name(),
                transaction.timestamp().toString(),
                transaction.description()
        );
    }

    /**
     * Convert JSON DTO back to domain Transaction.
     */
    public Transaction toDomain() {
        return new Transaction(
                TransactionId.of(id),
                AccountId.of(sourceAccountId),
                targetAccountId != null ? AccountId.of(targetAccountId) : null,
                Money.of(new BigDecimal(amount), Currency.valueOf(currency)),
                TransactionType.valueOf(type),
                Instant.parse(timestamp),
                description
        );
    }
}
