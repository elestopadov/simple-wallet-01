package com.github.wallet.infrastructure.config;

import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Configuration holder for the wallet application.
 * Provides configurable parameters for fraud detection, persistence, etc.
 * Final class — immutable configuration.
 */
public final class WalletConfiguration {

    private final Money singleTransactionLimit;
    private final Path dataDirectory;
    private final boolean persistenceEnabled;

    private WalletConfiguration(final Money singleTransactionLimit,
                                final Path dataDirectory,
                                final boolean persistenceEnabled) {
        this.singleTransactionLimit = Objects.requireNonNull(singleTransactionLimit);
        this.dataDirectory = Objects.requireNonNull(dataDirectory);
        this.persistenceEnabled = persistenceEnabled;
    }

    /**
     * Create default configuration suitable for production use.
     */
    public static WalletConfiguration defaultConfig() {
        return new WalletConfiguration(
                Money.of(BigDecimal.valueOf(10000), Currency.USD),
                Path.of("data"),
                true
        );
    }

    /**
     * Create configuration for testing (no file persistence, higher limits).
     */
    public static WalletConfiguration testConfig() {
        return new WalletConfiguration(
                Money.of(BigDecimal.valueOf(1000000), Currency.USD),
                Path.of("test-data"),
                false
        );
    }

    /**
     * Builder-style method to create custom configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    public Money getSingleTransactionLimit() {
        return singleTransactionLimit;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }

    @Override
    public String toString() {
        return "WalletConfiguration{limit=%s, dataDir=%s, persistence=%s}"
                .formatted(singleTransactionLimit, dataDirectory, persistenceEnabled);
    }

    /**
     * Builder for WalletConfiguration.
     */
    public static final class Builder {

        private Money singleTransactionLimit = Money.of(BigDecimal.valueOf(10000), Currency.USD);
        private Path dataDirectory = Path.of("data");
        private boolean persistenceEnabled = true;

        private Builder() {
        }

        public Builder singleTransactionLimit(final Money limit) {
            this.singleTransactionLimit = Objects.requireNonNull(limit);
            return this;
        }

        public Builder dataDirectory(final Path directory) {
            this.dataDirectory = Objects.requireNonNull(directory);
            return this;
        }

        public Builder persistenceEnabled(final boolean enabled) {
            this.persistenceEnabled = enabled;
            return this;
        }

        public WalletConfiguration build() {
            return new WalletConfiguration(singleTransactionLimit, dataDirectory, persistenceEnabled);
        }
    }
}
