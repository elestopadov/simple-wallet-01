package com.github.wallet.application.dto;

import com.github.wallet.domain.model.Transaction;
import com.github.wallet.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-only DTO representing transaction information for presentation layer.
 */
public record TransactionInfo(
        String id,
        String sourceAccountId,
        String targetAccountId,
        BigDecimal amount,
        String currency,
        TransactionType type,
        Instant timestamp,
        String description
) {

    /**
     * Factory method to create TransactionInfo from a domain Transaction.
     */
    public static TransactionInfo fromDomain(final Transaction transaction) {
        return new TransactionInfo(
                transaction.id().value(),
                transaction.sourceAccountId().value(),
                transaction.targetAccountId() != null ? transaction.targetAccountId().value() : null,
                transaction.amount().amount(),
                transaction.amount().currency().name(),
                transaction.type(),
                transaction.timestamp(),
                transaction.description()
        );
    }
}
