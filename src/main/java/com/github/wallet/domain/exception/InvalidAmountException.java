package com.github.wallet.domain.exception;

import com.github.wallet.domain.model.Money;

/**
 * Thrown when an invalid monetary amount is provided (negative, zero, etc.).
 */
public final class InvalidAmountException extends DomainException {

    private static final String ERROR_CODE = "INVALID_AMOUNT";

    public InvalidAmountException(final Money amount) {
        super("Invalid amount: %s. Amount must be positive.".formatted(amount), ERROR_CODE);
    }

    public InvalidAmountException(final String reason) {
        super("Invalid amount: %s".formatted(reason), ERROR_CODE);
    }
}
