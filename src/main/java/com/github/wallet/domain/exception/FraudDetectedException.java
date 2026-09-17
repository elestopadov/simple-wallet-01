package com.github.wallet.domain.exception;

import com.github.wallet.domain.model.Money;

/**
 * Thrown when a potential fraudulent transaction is detected.
 */
public final class FraudDetectedException extends DomainException {

    private static final String ERROR_CODE = "FRAUD_DETECTED";

    public FraudDetectedException(final Money amount, final Money limit) {
        super("Potential fraud detected: transaction amount %s exceeds single transaction limit %s"
                .formatted(amount, limit), ERROR_CODE);
    }
}
