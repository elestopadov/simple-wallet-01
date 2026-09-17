package com.github.wallet.domain.service;

import com.github.wallet.domain.exception.FraudDetectedException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Domain service for detecting potentially fraudulent transactions.
 * Validates transaction amounts against configurable limits.
 * Final class — not designed for inheritance.
 */
public final class FraudDetectionService {

    private final Money singleTransactionLimit;

    /**
     * Creates a FraudDetectionService with a single-transaction limit.
     *
     * @param singleTransactionLimit maximum allowed amount for a single transaction
     */
    public FraudDetectionService(final Money singleTransactionLimit) {
        this.singleTransactionLimit = Objects.requireNonNull(singleTransactionLimit,
                "Single transaction limit must not be null");
        if (!singleTransactionLimit.isPositive()) {
            throw new IllegalArgumentException("Single transaction limit must be positive: " + singleTransactionLimit);
        }
    }

    /**
     * Validates that the transaction amount does not exceed the configured limit.
     *
     * @param amount  the transaction amount to validate
     * @param account the account performing the transaction (for context/logging)
     * @throws FraudDetectedException if the amount exceeds the limit
     */
    public void validateTransaction(final Money amount, final Account account) {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(account, "Account must not be null");

        if (amount.isGreaterThan(singleTransactionLimit)) {
            throw new FraudDetectedException(amount, singleTransactionLimit);
        }
    }

    /**
     * Returns the configured single transaction limit.
     */
    public Money getSingleTransactionLimit() {
        return singleTransactionLimit;
    }
}
