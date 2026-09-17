package com.github.wallet.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Command DTO representing a transfer request between accounts.
 */
public record TransferRequest(
        String fromOwnerName,
        String toOwnerName,
        BigDecimal amount
) {

    public TransferRequest {
        Objects.requireNonNull(fromOwnerName, "Source owner name must not be null");
        Objects.requireNonNull(toOwnerName, "Target owner name must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");
        if (fromOwnerName.isBlank()) {
            throw new IllegalArgumentException("Source owner name must not be blank");
        }
        if (toOwnerName.isBlank()) {
            throw new IllegalArgumentException("Target owner name must not be blank");
        }
        if (fromOwnerName.equalsIgnoreCase(toOwnerName)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
    }
}
