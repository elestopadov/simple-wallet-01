package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.port.AccountRepository;

import java.util.List;
import java.util.Objects;

/**
 * Use case: List all existing accounts.
 */
public final class ListAccountsUseCase {

    private final AccountRepository accountRepository;

    public ListAccountsUseCase(final AccountRepository accountRepository) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    /**
     * Retrieve information about all accounts.
     */
    public List<AccountInfo> execute() {
        return accountRepository.findAll()
                .stream()
                .map(AccountInfo::fromDomain)
                .toList();
    }
}
