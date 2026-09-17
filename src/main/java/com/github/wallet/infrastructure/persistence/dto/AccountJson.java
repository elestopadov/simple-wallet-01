package com.github.wallet.infrastructure.persistence.dto;

import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.AccountStatus;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JSON-serializable DTO for Account persistence.
 * Maps between domain Account entity and JSON representation.
 */
public record AccountJson(
        String id,
        String ownerName,
        String balance,
        String currency,
        String status,
        String createdAt
) {

    /**
     * Convert domain Account to JSON DTO.
     */
    public static AccountJson fromDomain(final Account account) {
        return new AccountJson(
                account.getId().value(),
                account.getOwnerName(),
                account.getBalance().amount().toPlainString(),
                account.getCurrency().name(),
                account.getStatus().name(),
                account.getCreatedAt().toString()
        );
    }

    /**
     * Convert JSON DTO back to domain Account.
     */
    public Account toDomain() {
        final Currency curr = Currency.valueOf(currency);
        return Account.reconstitute(
                AccountId.of(id),
                ownerName,
                curr,
                Money.of(new BigDecimal(balance), curr),
                AccountStatus.valueOf(status),
                Instant.parse(createdAt)
        );
    }
}
