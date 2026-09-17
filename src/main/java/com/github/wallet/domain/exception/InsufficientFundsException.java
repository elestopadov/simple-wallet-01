package com.github.wallet.domain.exception;

import com.github.wallet.domain.model.Money;

/**
 * Thrown when an account does not have sufficient funds for the requested operation.
 */
public final class InsufficientFundsException extends DomainException {

    private static final String ERROR_CODE = "INSUFFICIENT_FUNDS";

    public InsufficientFundsException(final Money balance, final Money requested) {
        super("Insufficient funds: balance=%s, requested=%s".formatted(balance, requested), ERROR_CODE);
    }
}
