package com.github.wallet.cli.formatter;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.application.dto.TransactionInfo;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Formats output for the CLI presentation layer.
 * Final class — utility, not designed for inheritance.
 */
public final class OutputFormatter {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final String SEPARATOR = "─".repeat(60);
    private static final String DOUBLE_SEPARATOR = "═".repeat(60);

    private OutputFormatter() {
        // utility class
    }

    /**
     * Format a single account info for display.
     */
    public static String formatAccount(final AccountInfo account) {
        return """
                ┌%s┐
                │ Account: %-49s│
                │ ID:      %-49s│
                │ Balance: %-49s│
                │ Status:  %-49s│
                │ Created: %-49s│
                └%s┘""".formatted(
                SEPARATOR,
                account.ownerName(),
                account.id(),
                account.formattedBalance() + " " + account.currency().name(),
                account.status().name(),
                DATE_FORMAT.format(account.createdAt()),
                SEPARATOR
        );
    }

    /**
     * Format a list of accounts as a table.
     */
    public static String formatAccountList(final List<AccountInfo> accounts) {
        if (accounts.isEmpty()) {
            return "  No accounts found.";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("  %-15s %-12s %-10s %-20s%n".formatted("OWNER", "BALANCE", "CURRENCY", "STATUS"));
        sb.append("  ").append(SEPARATOR).append("\n");

        for (final AccountInfo account : accounts) {
            sb.append("  %-15s %-12s %-10s %-20s%n".formatted(
                    account.ownerName(),
                    account.formattedBalance(),
                    account.currency().name(),
                    account.status().name()
            ));
        }
        return sb.toString();
    }

    /**
     * Format transaction history.
     */
    public static String formatTransactionHistory(final String ownerName, final List<TransactionInfo> transactions) {
        if (transactions.isEmpty()) {
            return "  No transactions found for '%s'.".formatted(ownerName);
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("  Transaction history for '%s' (%d transactions):%n".formatted(ownerName, transactions.size()));
        sb.append("  ").append(SEPARATOR).append("\n");
        sb.append("  %-20s %-15s %-12s %-15s%n".formatted("TIMESTAMP", "TYPE", "AMOUNT", "DESCRIPTION"));
        sb.append("  ").append(SEPARATOR).append("\n");

        for (final TransactionInfo tx : transactions) {
            sb.append("  %-20s %-15s %-12s %-15s%n".formatted(
                    DATE_FORMAT.format(tx.timestamp()),
                    tx.type().name(),
                    tx.amount().toPlainString() + " " + tx.currency(),
                    truncate(tx.description(), 30)
            ));
        }
        return sb.toString();
    }

    /**
     * Format a success message.
     */
    public static String success(final String message) {
        return "  [OK] " + message;
    }

    /**
     * Format an error message.
     */
    public static String error(final String message) {
        return "  [ERROR] " + message;
    }

    /**
     * Format the help text.
     */
    public static String helpText() {
        return """
                
                %s
                            Simple Wallet - Digital Banking CLI
                %s
                
                  COMMANDS:
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                  %-45s %s
                
                %s
                """.formatted(
                DOUBLE_SEPARATOR,
                DOUBLE_SEPARATOR,
                "create-account <name> <currency>", "Create a new account",
                "deposit <name> <amount>", "Deposit money",
                "withdraw <name> <amount>", "Withdraw money",
                "transfer <from> <to> <amount>", "Transfer between accounts",
                "balance <name>", "Check account balance",
                "history <name>", "View transaction history",
                "list-accounts", "List all accounts",
                "close-account <name>", "Close an account",
                "help", "Show this help message",
                "clear", "Clear the screen",
                "exit", "Exit the application",
                DOUBLE_SEPARATOR
        );
    }

    /**
     * Welcome banner.
     */
    public static String welcomeBanner() {
        return """
                
                %s
                       Simple Wallet v1.0.0 - Digital Banking CLI
                       Type 'help' for available commands.
                %s
                """.formatted(DOUBLE_SEPARATOR, DOUBLE_SEPARATOR);
    }

    private static String truncate(final String text, final int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "";
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
