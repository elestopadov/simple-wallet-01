package com.github.wallet.domain.exception;

/**
 * Thrown when a referenced account cannot be found.
 */
public final class AccountNotFoundException extends DomainException {

    private static final String ERROR_CODE = "ACCOUNT_NOT_FOUND";

    public AccountNotFoundException(final String ownerName) {
        super("Account not found for owner: '%s'".formatted(ownerName), ERROR_CODE);
    }
}
