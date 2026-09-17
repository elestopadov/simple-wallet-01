package com.github.wallet.infrastructure.persistence;

public final class StorageException extends RuntimeException {
    public StorageException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
