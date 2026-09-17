package com.github.wallet.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a bank account.
 * Mutable state (balance changes), but with defensive validation.
 * Final class — not designed for inheritance.
 */
public final class Account {

    private final AccountId id;
    private final String ownerName;
    private final Currency currency;
    private final Instant createdAt;
    private Money balance;
    private AccountStatus status;

    private Account(final AccountId id,
                    final String ownerName,
                    final Currency currency,
                    final Money balance,
                    final AccountStatus status,
                    final Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Account ID must not be null");
        this.ownerName = validateOwnerName(ownerName);
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");
        this.balance = Objects.requireNonNull(balance, "Balance must not be null");
        this.status = Objects.requireNonNull(status, "Status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");

        if (balance.currency() != currency) {
            throw new IllegalArgumentException("Balance currency must match account currency");
        }
    }

    /**
     * Factory method to create a new active account with zero balance.
     */
    public static Account create(final String ownerName, final Currency currency) {
        return new Account(
                AccountId.generate(),
                ownerName,
                currency,
                Money.zero(currency),
                AccountStatus.ACTIVE,
                Instant.now()
        );
    }

    /**
     * Factory method to reconstitute an account from persistence.
     */
    public static Account reconstitute(final AccountId id,
                                       final String ownerName,
                                       final Currency currency,
                                       final Money balance,
                                       final AccountStatus status,
                                       final Instant createdAt) {
        return new Account(id, ownerName, currency, balance, status, createdAt);
    }

    /**
     * Deposit money into this account.
     *
     * @throws IllegalStateException if account is closed
     * @throws IllegalArgumentException if amount is not positive or currency mismatches
     */
    public void deposit(final Money amount) {
        assertActive();
        assertPositiveAmount(amount);
        assertMatchingCurrency(amount);
        this.balance = this.balance.add(amount);
    }

    /**
     * Withdraw money from this account.
     *
     * @throws IllegalStateException if account is closed
     * @throws IllegalArgumentException if amount is not positive, currency mismatches, or insufficient funds
     */
    public void withdraw(final Money amount) {
        assertActive();
        assertPositiveAmount(amount);
        assertMatchingCurrency(amount);
        assertSufficientFunds(amount);
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Close this account. Only possible if balance is zero.
     *
     * @throws IllegalStateException if account is already closed or has non-zero balance
     */
    public void close() {
        assertActive();
        if (!balance.isZero()) {
            throw new IllegalStateException(
                    "Cannot close account with non-zero balance: " + balance);
        }
        this.status = AccountStatus.CLOSED;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isClosed() {
        return status == AccountStatus.CLOSED;
    }

    // --- Getters ---

    public AccountId getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // --- Private helpers ---

    private void assertActive() {
        if (isClosed()) {
            throw new IllegalStateException("Account is closed: " + id);
        }
    }

    private void assertPositiveAmount(final Money amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive: " + amount);
        }
    }

    private void assertMatchingCurrency(final Money amount) {
        if (amount.currency() != this.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: account is %s but got %s".formatted(this.currency, amount.currency()));
        }
    }

    private void assertSufficientFunds(final Money amount) {
        if (amount.isGreaterThan(this.balance)) {
            throw new IllegalStateException(
                    "Insufficient funds: balance=%s, requested=%s".formatted(this.balance, amount));
        }
    }

    private static String validateOwnerName(final String ownerName) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");
        final String trimmed = ownerName.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Owner name must not be blank");
        }
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Owner name must not exceed 100 characters");
        }
        return trimmed;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Account{id=%s, owner='%s', balance=%s, status=%s}".formatted(id, ownerName, balance, status);
    }
}
