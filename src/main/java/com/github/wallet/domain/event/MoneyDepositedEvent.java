package com.github.wallet.domain.event;

import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.Money;

import java.time.Instant;

/**
 * Event published when money is deposited into an account.
 */
public record MoneyDepositedEvent(
        AccountId accountId,
        Money amount,
        Money newBalance,
        Instant occurredAt
) implements DomainEvent {

    public MoneyDepositedEvent(final AccountId accountId, final Money amount, final Money newBalance) {
        this(accountId, amount, newBalance, Instant.now());
    }

    @Override
    public String description() {
        return "Deposited %s into account %s. New balance: %s".formatted(amount, accountId, newBalance);
    }
}
