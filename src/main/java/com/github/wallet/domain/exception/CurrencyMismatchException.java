package com.github.wallet.domain.exception;

import com.github.wallet.domain.model.Currency;

/**
 * Thrown when an operation is attempted between accounts with different currencies.
 */
public final class CurrencyMismatchException extends DomainException {

    private static final String ERROR_CODE = "CURRENCY_MISMATCH";

    public CurrencyMismatchException(final Currency source, final Currency target) {
        super("Currency mismatch: cannot transfer between %s and %s accounts".formatted(source, target), ERROR_CODE);
    }
}
