package com.github.wallet.cli;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.application.dto.TransactionInfo;
import com.github.wallet.application.dto.TransferRequest;
import com.github.wallet.application.usecase.CloseAccountUseCase;
import com.github.wallet.application.usecase.CreateAccountUseCase;
import com.github.wallet.application.usecase.DepositMoneyUseCase;
import com.github.wallet.application.usecase.GetBalanceUseCase;
import com.github.wallet.application.usecase.GetTransactionHistoryUseCase;
import com.github.wallet.application.usecase.ListAccountsUseCase;
import com.github.wallet.application.usecase.TransferMoneyUseCase;
import com.github.wallet.application.usecase.WithdrawMoneyUseCase;
import com.github.wallet.cli.formatter.OutputFormatter;
import com.github.wallet.domain.exception.DomainException;
import com.github.wallet.domain.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Routes CLI commands to appropriate use cases.
 * Parses user input and delegates to the application layer.
 * Final class — not designed for inheritance.
 */
public final class CommandRouter {

    private static final Logger logger = LoggerFactory.getLogger(CommandRouter.class);

    private final CreateAccountUseCase createAccountUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final ListAccountsUseCase listAccountsUseCase;
    private final CloseAccountUseCase closeAccountUseCase;

    public CommandRouter(final CreateAccountUseCase createAccountUseCase,
                         final DepositMoneyUseCase depositMoneyUseCase,
                         final WithdrawMoneyUseCase withdrawMoneyUseCase,
                         final TransferMoneyUseCase transferMoneyUseCase,
                         final GetBalanceUseCase getBalanceUseCase,
                         final GetTransactionHistoryUseCase getTransactionHistoryUseCase,
                         final ListAccountsUseCase listAccountsUseCase,
                         final CloseAccountUseCase closeAccountUseCase) {
        this.createAccountUseCase = Objects.requireNonNull(createAccountUseCase);
        this.depositMoneyUseCase = Objects.requireNonNull(depositMoneyUseCase);
        this.withdrawMoneyUseCase = Objects.requireNonNull(withdrawMoneyUseCase);
        this.transferMoneyUseCase = Objects.requireNonNull(transferMoneyUseCase);
        this.getBalanceUseCase = Objects.requireNonNull(getBalanceUseCase);
        this.getTransactionHistoryUseCase = Objects.requireNonNull(getTransactionHistoryUseCase);
        this.listAccountsUseCase = Objects.requireNonNull(listAccountsUseCase);
        this.closeAccountUseCase = Objects.requireNonNull(closeAccountUseCase);
    }

    /**
     * Route user input to the appropriate command handler.
     *
     * @param input raw user input
     * @return formatted output string, or null if exit command
     */
    public String route(final String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        final String[] parts = input.trim().split("\\s+");
        final String command = parts[0].toLowerCase();

        try {
            return switch (command) {
                case "create-account" -> handleCreateAccount(parts);
                case "deposit" -> handleDeposit(parts);
                case "withdraw" -> handleWithdraw(parts);
                case "transfer" -> handleTransfer(parts);
                case "balance" -> handleBalance(parts);
                case "history" -> handleHistory(parts);
                case "list-accounts" -> handleListAccounts();
                case "close-account" -> handleCloseAccount(parts);
                case "help" -> OutputFormatter.helpText();
                case "clear" -> "\033[H\033[2J";
                case "exit" -> null;
                default -> OutputFormatter.error("Unknown command: '%s'. Type 'help' for available commands.".formatted(command));
            };
        } catch (final DomainException e) {
            logger.warn("Domain error: {}", e.getMessage());
            return OutputFormatter.error(e.getMessage());
        } catch (final IllegalArgumentException e) {
            logger.warn("Validation error: {}", e.getMessage());
            return OutputFormatter.error(e.getMessage());
        } catch (final Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return OutputFormatter.error("Unexpected error: " + e.getMessage());
        }
    }

    private String handleCreateAccount(final String[] parts) {
        if (parts.length < 3) {
            return OutputFormatter.error("Usage: create-account <name> <currency>\n  Supported currencies: USD, EUR, GBP, RUB");
        }
        final String name = parts[1];
        final Currency currency = parseCurrency(parts[2]);
        final AccountInfo info = createAccountUseCase.execute(name, currency);
        return OutputFormatter.success("Account created for '%s' with currency %s\n%s".formatted(
                name, currency.name(), OutputFormatter.formatAccount(info)));
    }

    private String handleDeposit(final String[] parts) {
        if (parts.length < 3) {
            return OutputFormatter.error("Usage: deposit <name> <amount>");
        }
        final String name = parts[1];
        final BigDecimal amount = parseAmount(parts[2]);
        final AccountInfo info = depositMoneyUseCase.execute(name, amount);
        return OutputFormatter.success("Deposited %s %s into '%s'. New balance: %s".formatted(
                amount.toPlainString(), info.currency().name(), name, info.formattedBalance()));
    }

    private String handleWithdraw(final String[] parts) {
        if (parts.length < 3) {
            return OutputFormatter.error("Usage: withdraw <name> <amount>");
        }
        final String name = parts[1];
        final BigDecimal amount = parseAmount(parts[2]);
        final AccountInfo info = withdrawMoneyUseCase.execute(name, amount);
        return OutputFormatter.success("Withdrew %s %s from '%s'. New balance: %s".formatted(
                amount.toPlainString(), info.currency().name(), name, info.formattedBalance()));
    }

    private String handleTransfer(final String[] parts) {
        if (parts.length < 4) {
            return OutputFormatter.error("Usage: transfer <from> <to> <amount>");
        }
        final String from = parts[1];
        final String to = parts[2];
        final BigDecimal amount = parseAmount(parts[3]);
        final TransferRequest request = new TransferRequest(from, to, amount);
        transferMoneyUseCase.execute(request);
        return OutputFormatter.success("Transferred %s from '%s' to '%s'.".formatted(
                amount.toPlainString(), from, to));
    }

    private String handleBalance(final String[] parts) {
        if (parts.length < 2) {
            return OutputFormatter.error("Usage: balance <name>");
        }
        final String name = parts[1];
        final AccountInfo info = getBalanceUseCase.execute(name);
        return OutputFormatter.formatAccount(info);
    }

    private String handleHistory(final String[] parts) {
        if (parts.length < 2) {
            return OutputFormatter.error("Usage: history <name>");
        }
        final String name = parts[1];
        final List<TransactionInfo> history = getTransactionHistoryUseCase.execute(name);
        return OutputFormatter.formatTransactionHistory(name, history);
    }

    private String handleListAccounts() {
        final List<AccountInfo> accounts = listAccountsUseCase.execute();
        return OutputFormatter.formatAccountList(accounts);
    }

    private String handleCloseAccount(final String[] parts) {
        if (parts.length < 2) {
            return OutputFormatter.error("Usage: close-account <name>");
        }
        final String name = parts[1];
        closeAccountUseCase.execute(name);
        return OutputFormatter.success("Account '%s' has been closed.".formatted(name));
    }

    private static Currency parseCurrency(final String value) {
        try {
            return Currency.valueOf(value.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid currency: '%s'. Supported: USD, EUR, GBP, RUB".formatted(value));
        }
    }

    private static BigDecimal parseAmount(final String value) {
        try {
            final BigDecimal amount = new BigDecimal(value);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive: " + value);
            }
            return amount;
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: '%s'. Please provide a numeric value.".formatted(value));
        }
    }
}
