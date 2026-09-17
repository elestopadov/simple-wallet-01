package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.event.AccountCreatedEvent;
import com.github.wallet.domain.exception.DuplicateAccountException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.port.AccountRepository;
import com.github.wallet.domain.port.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Use case: Create a new wallet account.
 */
public final class CreateAccountUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CreateAccountUseCase.class);

    private final AccountRepository accountRepository;
    private final EventPublisher eventPublisher;

    public CreateAccountUseCase(final AccountRepository accountRepository,
                                final EventPublisher eventPublisher) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    /**
     * Create a new account with the given owner name and currency.
     *
     * @throws DuplicateAccountException if an account with the same name already exists
     */
    public AccountInfo execute(final String ownerName, final Currency currency) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");
        Objects.requireNonNull(currency, "Currency must not be null");

        if (accountRepository.existsByOwnerName(ownerName)) {
            throw new DuplicateAccountException(ownerName);
        }

        final Account account = Account.create(ownerName, currency);
        accountRepository.save(account);

        logger.info("Account created: owner='{}', currency={}, id={}", ownerName, currency, account.getId());
        eventPublisher.publish(new AccountCreatedEvent(account.getId(), ownerName, currency));

        return AccountInfo.fromDomain(account);
    }
}
