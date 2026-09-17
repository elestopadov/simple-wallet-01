package com.github.wallet.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object representing a monetary amount with currency.
 * Immutable, uses BigDecimal for precision in financial calculations.
 */
public record Money(BigDecimal amount, Currency currency) {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public Money {
        Objects.requireNonNull(amount, "Amount must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");
        amount = amount.setScale(SCALE, ROUNDING);
    }

    /**
     * Factory method for convenient creation.
     */
    public static Money of(final BigDecimal amount, final Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Convenience factory for tests, CLI examples and simple demos.
     * Financial calculations still use BigDecimal internally.
     */
    public static Money of(final int amount, final Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Convenience factory for tests, CLI examples and simple demos.
     * BigDecimal.valueOf avoids binary floating point surprises.
     */
    public static Money of(final double amount, final Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates zero money for the given currency.
     */
    public static Money zero(final Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Add another Money amount. Both must have the same currency.
     *
     * @throws IllegalArgumentException if currencies differ
     */
    public Money add(final Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtract another Money amount. Both must have the same currency.
     *
     * @throws IllegalArgumentException if currencies differ
     */
    public Money subtract(final Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Returns true if the amount is negative.
     */
    public boolean isNegative() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Returns true if the amount is zero.
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Returns true if the amount is positive (greater than zero).
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns true if this money is greater than the other.
     */
    public boolean isGreaterThan(final Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Returns true if this money is greater than or equal to the other.
     */
    public boolean isGreaterThanOrEqual(final Money other) {
        assertSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    private void assertSameCurrency(final Money other) {
        Objects.requireNonNull(other, "Other money must not be null");
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: cannot operate on %s and %s".formatted(this.currency, other.currency));
        }
    }

    @Override
    public String toString() {
        return "%s %s".formatted(amount.toPlainString(), currency.name());
    }

    /**
     * Formatted display string with currency symbol.
     */
    public String toDisplayString() {
        return "%s%s".formatted(currency.getSymbol(), amount.toPlainString());
    }
}
