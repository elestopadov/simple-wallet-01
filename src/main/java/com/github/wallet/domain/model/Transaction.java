package com.github.wallet.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record representing a completed financial transaction.
 * Once created, a transaction cannot be modified — it is a historical fact.
 */
public record Transaction(
        TransactionId id,
        AccountId sourceAccountId,
        AccountId targetAccountId,
        Money amount,
        TransactionType type,
        Instant timestamp,
        String description
) {

    public Transaction {
        Objects.requireNonNull(id, "Transaction ID must not be null");
        Objects.requireNonNull(sourceAccountId, "Source account ID must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(type, "Transaction type must not be null");
        Objects.requireNonNull(timestamp, "Timestamp must not be null");
        if (description == null) {
            description = "";
        }
    }

    /**
     * Factory for deposit transactions.
     */
    public static Transaction deposit(final AccountId accountId, final Money amount) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                null,
                amount,
                TransactionType.DEPOSIT,
                Instant.now(),
                "Deposit of " + amount
        );
    }

    /**
     * Factory for withdrawal transactions.
     */
    public static Transaction withdrawal(final AccountId accountId, final Money amount) {
        return new Transaction(
                TransactionId.generate(),
                accountId,
                null,
                amount,
                TransactionType.WITHDRAWAL,
                Instant.now(),
                "Withdrawal of " + amount
        );
    }

    /**
     * Factory for the debit side of a transfer (source account).
     */
    public static Transaction transferDebit(final AccountId sourceId, final AccountId targetId, final Money amount) {
        return new Transaction(
                TransactionId.generate(),
                sourceId,
                targetId,
                amount,
                TransactionType.TRANSFER_DEBIT,
                Instant.now(),
                "Transfer to " + targetId.value() + " of " + amount
        );
    }

    /**
     * Factory for the credit side of a transfer (target account).
     */
    public static Transaction transferCredit(final AccountId sourceId, final AccountId targetId, final Money amount) {
        return new Transaction(
                TransactionId.generate(),
                targetId,
                sourceId,
                amount,
                TransactionType.TRANSFER_CREDIT,
                Instant.now(),
                "Transfer from " + sourceId.value() + " of " + amount
        );
    }

    /**
     * Check if this transaction involves the given account (as source or target).
     */
    public boolean involvesAccount(final AccountId accountId) {
        return sourceAccountId.equals(accountId)
                || (targetAccountId != null && targetAccountId.equals(accountId));
    }
}
