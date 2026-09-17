package com.github.wallet.infrastructure.persistence;

import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.port.AccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of AccountRepository.
 * Uses ConcurrentHashMap for thread-safety in demo scenarios.
 */
public final class InMemoryAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void save(final Account account) {
        Objects.requireNonNull(account, "Account must not be null");
        accounts.put(account.getId(), account);
    }

    @Override
    public Optional<Account> findById(final AccountId id) {
        Objects.requireNonNull(id, "Account ID must not be null");
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Optional<Account> findByOwnerName(final String ownerName) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");
        return accounts.values().stream()
                .filter(account -> account.getOwnerName().equalsIgnoreCase(ownerName.trim()))
                .findFirst();
    }

    @Override
    public boolean existsByOwnerName(final String ownerName) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");
        return accounts.values().stream()
                .anyMatch(account -> account.getOwnerName().equalsIgnoreCase(ownerName.trim()));
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public void deleteById(final AccountId id) {
        Objects.requireNonNull(id, "Account ID must not be null");
        accounts.remove(id);
    }

    /**
     * Clear all accounts (useful for testing).
     */
    public void clear() {
        accounts.clear();
    }

    /**
     * Get the count of stored accounts.
     */
    public int count() {
        return accounts.size();
    }
}
