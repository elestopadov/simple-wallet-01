package com.github.wallet.application.usecase;

import com.github.wallet.domain.event.AccountClosedEvent;
import com.github.wallet.domain.exception.AccountClosedException;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.port.AccountRepository;
import com.github.wallet.domain.port.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Use case: Close an existing account (balance must be zero).
 */
public final class CloseAccountUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CloseAccountUseCase.class);

    private final AccountRepository accountRepository;
    private final EventPublisher eventPublisher;

    public CloseAccountUseCase(final AccountRepository accountRepository,
                               final EventPublisher eventPublisher) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    /**
     * Close the named account. Balance must be zero.
     *
     * @throws AccountNotFoundException if account does not exist
     * @throws AccountClosedException if account is already closed
     * @throws IllegalStateException if account has non-zero balance
     */
    public void execute(final String ownerName) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");

        final Account account = accountRepository.findByOwnerName(ownerName)
                .orElseThrow(() -> new AccountNotFoundException(ownerName));

        if (account.isClosed()) {
            throw new AccountClosedException(account.getId());
        }

        account.close();
        accountRepository.save(account);

        logger.info("Account closed: owner='{}'", ownerName);
        eventPublisher.publish(new AccountClosedEvent(account.getId(), ownerName));
    }
}
