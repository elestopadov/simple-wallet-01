package com.github.wallet.cli;

import com.github.wallet.infrastructure.config.WalletConfiguration;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WalletApp End-to-End Integration Test")
class WalletAppIT {

    @TempDir
    Path tempDir;

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
    }

    private WalletApp createApp(final String... commands) {
        final String input = String.join("\n", commands) + "\n";
        final Scanner scanner = new Scanner(input);
        final PrintStream printStream = new PrintStream(outputStream);
        final WalletConfiguration config = WalletConfiguration.builder()
                .singleTransactionLimit(Money.of(BigDecimal.valueOf(10000), Currency.USD))
                .dataDirectory(tempDir)
                .persistenceEnabled(false)
                .build();
        return new WalletApp(config, scanner, printStream);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    @Nested
    @DisplayName("Full Workflow")
    class FullWorkflow {

        @Test
        @DisplayName("should execute create → deposit → withdraw → balance flow")
        void shouldExecuteFullFlow() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "deposit Alice 1000",
                    "withdraw Alice 300",
                    "balance Alice",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("Account created for 'Alice'");
            assertThat(output).contains("Deposited");
            assertThat(output).contains("1000");
            assertThat(output).contains("Withdrew");
            assertThat(output).contains("300");
            assertThat(output).contains("700.00");
            assertThat(output).contains("Goodbye");
        }

        @Test
        @DisplayName("should execute create → transfer flow")
        void shouldExecuteTransferFlow() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "create-account Bob USD",
                    "deposit Alice 5000",
                    "transfer Alice Bob 2000",
                    "balance Alice",
                    "balance Bob",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("Transferred");
            assertThat(output).contains("3000.00");
            assertThat(output).contains("2000.00");
        }

        @Test
        @DisplayName("should show transaction history")
        void shouldShowTransactionHistory() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "deposit Alice 500",
                    "deposit Alice 300",
                    "withdraw Alice 100",
                    "history Alice",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("Transaction history for 'Alice'");
            assertThat(output).contains("3 transactions");
            assertThat(output).contains("DEPOSIT");
            assertThat(output).contains("WITHDRAWAL");
        }

        @Test
        @DisplayName("should list all accounts")
        void shouldListAllAccounts() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "create-account Bob EUR",
                    "list-accounts",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("Alice");
            assertThat(output).contains("Bob");
            assertThat(output).contains("USD");
            assertThat(output).contains("EUR");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should handle unknown command gracefully")
        void shouldHandleUnknownCommand() {
            final WalletApp app = createApp(
                    "unknown-command",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("[ERROR]");
            assertThat(output).contains("Unknown command");
        }

        @Test
        @DisplayName("should handle duplicate account creation")
        void shouldHandleDuplicateAccount() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "create-account Alice EUR",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("[ERROR]");
            assertThat(output).contains("already exists");
        }

        @Test
        @DisplayName("should handle insufficient funds")
        void shouldHandleInsufficientFunds() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "deposit Alice 100",
                    "withdraw Alice 500",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("[ERROR]");
            assertThat(output).contains("Insufficient funds");
        }

        @Test
        @DisplayName("should handle non-existing account")
        void shouldHandleNonExistingAccount() {
            final WalletApp app = createApp(
                    "balance Ghost",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("[ERROR]");
            assertThat(output).contains("not found");
        }

        @Test
        @DisplayName("should handle invalid amount")
        void shouldHandleInvalidAmount() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "deposit Alice abc",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("[ERROR]");
            assertThat(output).contains("Invalid amount");
        }
    }

    @Nested
    @DisplayName("Close Account Flow")
    class CloseAccountFlow {

        @Test
        @DisplayName("should close account with zero balance")
        void shouldCloseAccountWithZeroBalance() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "close-account Alice",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("closed");
        }

        @Test
        @DisplayName("should prevent operations on closed account")
        void shouldPreventOperationsOnClosedAccount() {
            final WalletApp app = createApp(
                    "create-account Alice USD",
                    "close-account Alice",
                    "deposit Alice 100",
                    "exit"
            );

            app.run();
            final String output = getOutput();

            assertThat(output).contains("closed");
            assertThat(output).contains("[ERROR]");
        }
    }
}
