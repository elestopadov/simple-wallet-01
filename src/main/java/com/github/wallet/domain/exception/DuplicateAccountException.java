package com.github.wallet.domain.exception;

/**
 * Thrown when attempting to create an account with a name that already exists.
 */
public final class DuplicateAccountException extends DomainException {

    private static final String ERROR_CODE = "DUPLICATE_ACCOUNT";

    public DuplicateAccountException(final String ownerName) {
        super("Account already exists for owner: '%s'".formatted(ownerName), ERROR_CODE);
    }
}
