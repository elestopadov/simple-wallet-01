package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.TransactionInfo;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.port.AccountRepository;
import com.github.wallet.domain.port.TransactionRepository;

import java.util.List;
import java.util.Objects;

/**
 * Use case: Get transaction history for an account.
 */
public final class GetTransactionHistoryUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public GetTransactionHistoryUseCase(final AccountRepository accountRepository,
                                        final TransactionRepository transactionRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    /**
     * Get all transactions for the named account, sorted by timestamp (newest first).
     *
     * @throws AccountNotFoundException if account does not exist
     */
    public List<TransactionInfo> execute(final String ownerName) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");

        final Account account = accountRepository.findByOwnerName(ownerName)
                .orElseThrow(() -> new AccountNotFoundException(ownerName));

        return transactionRepository.findByAccountId(account.getId())
                .stream()
                .sorted((a, b) -> b.timestamp().compareTo(a.timestamp()))
                .map(TransactionInfo::fromDomain)
                .toList();
    }
}
