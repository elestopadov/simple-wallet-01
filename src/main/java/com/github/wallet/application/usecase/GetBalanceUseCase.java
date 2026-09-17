package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.port.AccountRepository;

import java.util.Objects;

/**
 * Use case: Get the current balance of an account.
 */
public final class GetBalanceUseCase {

    private final AccountRepository accountRepository;

    public GetBalanceUseCase(final AccountRepository accountRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    /**
     * Get account information (including balance) for the named owner.
     *
     * @throws AccountNotFoundException if account does not exist
     */
    public AccountInfo execute(final String ownerName) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");

        final Account account = accountRepository.findByOwnerName(ownerName)
                .orElseThrow(() -> new AccountNotFoundException(ownerName));

        return AccountInfo.fromDomain(account);
    }
}
