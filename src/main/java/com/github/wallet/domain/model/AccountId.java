package com.github.wallet.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique account identifier.
 * Immutable and UUID-based for global uniqueness.
 */
public record AccountId(String value) {

    public AccountId {
        Objects.requireNonNull(value, "Account ID value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Account ID value must not be blank");
        }
    }

    /**
     * Factory method to generate a new random AccountId.
     */
    public static AccountId generate() {
        return new AccountId(UUID.randomUUID().toString());
    }

    /**
     * Factory method to create AccountId from existing string value.
     */
    public static AccountId of(final String value) {
        return new AccountId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
