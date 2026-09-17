package com.github.wallet.domain.exception;

/**
 * Base class for all domain-level exceptions.
 * Provides a consistent error structure across the domain layer.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(final String message, final String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(final String message, final String errorCode, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
