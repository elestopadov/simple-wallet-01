package com.github.wallet.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique transaction identifier.
 * Immutable and UUID-based for global uniqueness.
 */
public record TransactionId(String value) {

    public TransactionId {
        Objects.requireNonNull(value, "Transaction ID value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Transaction ID value must not be blank");
        }
    }

    /**
     * Factory method to generate a new random TransactionId.
     */
    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID().toString());
    }

    /**
     * Factory method to create TransactionId from existing string value.
     */
    public static TransactionId of(final String value) {
        return new TransactionId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
