package com.github.wallet.infrastructure.persistence;

import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.Transaction;
import com.github.wallet.infrastructure.persistence.dto.AccountJson;
import com.github.wallet.infrastructure.persistence.dto.TransactionJson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JSON file-based persistence for accounts and transactions.
 * Provides save/load operations to/from JSON files.
 * Final class — not designed for inheritance.
 */
public final class JsonFileStorage {

    private static final Logger logger = LoggerFactory.getLogger(JsonFileStorage.class);

    private static final Type ACCOUNT_LIST_TYPE = new TypeToken<List<AccountJson>>() {}.getType();
    private static final Type TRANSACTION_LIST_TYPE = new TypeToken<List<TransactionJson>>() {}.getType();

    private final Gson gson;
    private final Path accountsFilePath;
    private final Path transactionsFilePath;

    public JsonFileStorage(final Path dataDirectory) {
        Objects.requireNonNull(dataDirectory, "Data directory must not be null");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.accountsFilePath = dataDirectory.resolve("accounts.json");
        this.transactionsFilePath = dataDirectory.resolve("transactions.json");

        ensureDirectoryExists(dataDirectory);
    }

    /**
     * Save all accounts to the JSON file.
     */
    public void saveAccounts(final List<Account> accounts) {
        Objects.requireNonNull(accounts, "Accounts list must not be null");
        final List<AccountJson> jsonList = accounts.stream()
                .map(AccountJson::fromDomain)
                .toList();
        writeToFile(accountsFilePath, gson.toJson(jsonList));
        logger.debug("Saved {} accounts to {}", accounts.size(), accountsFilePath);
    }

    /**
     * Load all accounts from the JSON file.
     *
     * @return list of accounts, empty list if file doesn't exist
     */
    public List<Account> loadAccounts() {
        final String json = readFromFile(accountsFilePath);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        final List<AccountJson> jsonList = parseJsonList(json, ACCOUNT_LIST_TYPE);
        if (jsonList == null) {
            return new ArrayList<>();
        }
        final List<Account> accounts = jsonList.stream()
                .map(AccountJson::toDomain)
                .toList();
        logger.debug("Loaded {} accounts from {}", accounts.size(), accountsFilePath);
        return accounts;
    }

    /**
     * Save all transactions to the JSON file.
     */
    public void saveTransactions(final List<Transaction> transactions) {
        Objects.requireNonNull(transactions, "Transactions list must not be null");
        final List<TransactionJson> jsonList = transactions.stream()
                .map(TransactionJson::fromDomain)
                .toList();
        writeToFile(transactionsFilePath, gson.toJson(jsonList));
        logger.debug("Saved {} transactions to {}", transactions.size(), transactionsFilePath);
    }

    /**
     * Load all transactions from the JSON file.
     *
     * @return list of transactions, empty list if file doesn't exist
     */
    public List<Transaction> loadTransactions() {
        final String json = readFromFile(transactionsFilePath);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        final List<TransactionJson> jsonList = parseJsonList(json, TRANSACTION_LIST_TYPE);
        if (jsonList == null) {
            return new ArrayList<>();
        }
        final List<Transaction> transactions = jsonList.stream()
                .map(TransactionJson::toDomain)
                .toList();
        logger.debug("Loaded {} transactions from {}", transactions.size(), transactionsFilePath);
        return transactions;
    }

    /**
     * Get the path to the accounts file.
     */
    public Path getAccountsFilePath() {
        return accountsFilePath;
    }

    /**
     * Get the path to the transactions file.
     */
    public Path getTransactionsFilePath() {
        return transactionsFilePath;
    }

    private <T> List<T> parseJsonList(final String json, final Type type) {
        try {
            final List<T> result = gson.fromJson(json, type);
            return result == null ? new ArrayList<>() : result;
        } catch (final JsonSyntaxException e) {
            logger.error("Invalid JSON storage format", e);
            throw new StorageException("Invalid JSON storage format", e);
        }
    }

    private void writeToFile(final Path filePath, final String content) {
        try {
            Path temp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            Files.move(temp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final IOException e) {
            logger.error("Failed to write to file: {}", filePath, e);
            throw new StorageException("Failed to write data to file: " + filePath, e);
        }
    }

    private String readFromFile(final Path filePath) {
        if (!Files.exists(filePath)) {
            return null;
        }
        try {
            return Files.readString(filePath);
        } catch (final IOException e) {
            logger.error("Failed to read from file: {}", filePath, e);
            throw new StorageException("Failed to read data from file: " + filePath, e);
        }
    }

    private void ensureDirectoryExists(final Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (final IOException e) {
            logger.error("Failed to create data directory: {}", directory, e);
            throw new StorageException("Failed to create data directory: " + directory, e);
        }
    }
}
