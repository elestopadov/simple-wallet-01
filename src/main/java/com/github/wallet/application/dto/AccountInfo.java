package com.github.wallet.application.dto;

import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.AccountStatus;
import com.github.wallet.domain.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-only DTO representing account information for presentation layer.
 */
public record AccountInfo(
        String id,
        String ownerName,
        BigDecimal balance,
        Currency currency,
        AccountStatus status,
        Instant createdAt
) {

    /**
     * Factory method to create AccountInfo from a domain Account entity.
     */
    public static AccountInfo fromDomain(final Account account) {
        return new AccountInfo(
                account.getId().value(),
                account.getOwnerName(),
                account.getBalance().amount(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }

    /**
     * Formatted balance string with currency symbol.
     */
    public String formattedBalance() {
        return "%s%s".formatted(currency.getSymbol(), balance.toPlainString());
    }
}
