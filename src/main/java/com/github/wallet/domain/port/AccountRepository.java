package com.github.wallet.domain.port;

import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.AccountId;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for account persistence.
 * Domain layer defines what it needs; infrastructure implements how.
 */
public interface AccountRepository {

    /**
     * Save or update an account.
     */
    void save(Account account);

    /**
     * Find an account by its unique ID.
     */
    Optional<Account> findById(AccountId id);

    /**
     * Find an account by owner name (case-insensitive).
     */
    Optional<Account> findByOwnerName(String ownerName);

    /**
     * Check if an account with the given owner name exists.
     */
    boolean existsByOwnerName(String ownerName);

    /**
     * Retrieve all accounts.
     */
    List<Account> findAll();

    /**
     * Delete an account by its ID.
     */
    void deleteById(AccountId id);
}
