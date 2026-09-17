package com.github.wallet.infrastructure.persistence;

import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.Transaction;
import com.github.wallet.domain.model.TransactionId;
import com.github.wallet.domain.port.TransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of TransactionRepository.
 * Uses ConcurrentHashMap for thread-safety in demo scenarios.
 */
public final class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<TransactionId, Transaction> transactions = new ConcurrentHashMap<>();

    @Override
    public void save(final Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction must not be null");
        transactions.put(transaction.id(), transaction);
    }

    @Override
    public Optional<Transaction> findById(final TransactionId id) {
        Objects.requireNonNull(id, "Transaction ID must not be null");
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public List<Transaction> findByAccountId(final AccountId accountId) {
        Objects.requireNonNull(accountId, "Account ID must not be null");
        return transactions.values().stream()
                .filter(tx -> tx.involvesAccount(accountId))
                .toList();
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }

    /**
     * Clear all transactions (useful for testing).
     */
    public void clear() {
        transactions.clear();
    }

    /**
     * Get the count of stored transactions.
     */
    public int count() {
        return transactions.size();
    }
}
