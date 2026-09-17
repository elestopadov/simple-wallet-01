package com.github.wallet.domain.model;

/**
 * Supported currencies in the wallet system.
 */
public enum Currency {
    USD("US Dollar", "$"),
    EUR("Euro", "\u20AC"),
    GBP("British Pound", "\u00A3"),
    RUB("Russian Ruble", "\u20BD");

    private final String displayName;
    private final String symbol;

    Currency(final String displayName, final String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }
}
