package com.github.wallet.domain.exception;

import com.github.wallet.domain.model.AccountId;

/**
 * Thrown when an operation is attempted on a closed account.
 */
public final class AccountClosedException extends DomainException {

    private static final String ERROR_CODE = "ACCOUNT_CLOSED";

    public AccountClosedException(final AccountId accountId) {
        super("Account is closed: %s".formatted(accountId), ERROR_CODE);
    }
}
