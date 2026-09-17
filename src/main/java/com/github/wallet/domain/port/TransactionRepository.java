package com.github.wallet.domain.port;

import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.Transaction;
import com.github.wallet.domain.model.TransactionId;

import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for transaction persistence.
 * Domain layer defines what it needs; infrastructure implements how.
 */
public interface TransactionRepository {

    /**
     * Save a transaction record.
     */
    void save(Transaction transaction);

    /**
     * Find a transaction by its unique ID.
     */
    Optional<Transaction> findById(TransactionId id);

    /**
     * Find all transactions involving the given account (as source or target).
     */
    List<Transaction> findByAccountId(AccountId accountId);

    /**
     * Retrieve all transactions.
     */
    List<Transaction> findAll();
}
