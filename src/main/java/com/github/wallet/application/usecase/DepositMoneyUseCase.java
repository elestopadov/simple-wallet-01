package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.event.MoneyDepositedEvent;
import com.github.wallet.domain.exception.AccountClosedException;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.exception.InvalidAmountException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.Money;
import com.github.wallet.domain.model.Transaction;
import com.github.wallet.domain.port.AccountRepository;
import com.github.wallet.domain.port.EventPublisher;
import com.github.wallet.domain.port.TransactionRepository;
import com.github.wallet.domain.service.FraudDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Use case: Deposit money into an account.
 */
public final class DepositMoneyUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DepositMoneyUseCase.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;
    private final FraudDetectionService fraudDetectionService;

    public DepositMoneyUseCase(final AccountRepository accountRepository,
                               final TransactionRepository transactionRepository,
                               final EventPublisher eventPublisher,
                               final FraudDetectionService fraudDetectionService) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.fraudDetectionService = Objects.requireNonNull(fraudDetectionService);
    }

    /**
     * Deposit the specified amount into the named account.
     *
     * @throws AccountNotFoundException if account does not exist
     * @throws AccountClosedException if account is closed
     * @throws InvalidAmountException if amount is not positive
     */
    public AccountInfo execute(final String ownerName, final BigDecimal amount) {
        Objects.requireNonNull(ownerName, "Owner name must not be null");
        Objects.requireNonNull(amount, "Amount must not be null");

        final Account account = accountRepository.findByOwnerName(ownerName)
                .orElseThrow(() -> new AccountNotFoundException(ownerName));

        if (account.isClosed()) {
            throw new AccountClosedException(account.getId());
        }

        final Money money = Money.of(amount, account.getCurrency());
        if (!money.isPositive()) {
            throw new InvalidAmountException(money);
        }

        fraudDetectionService.validateTransaction(money, account);

        account.deposit(money);
        accountRepository.save(account);

        final Transaction transaction = Transaction.deposit(account.getId(), money);
        transactionRepository.save(transaction);

        logger.info("Deposited {} into account '{}'", money, ownerName);
        eventPublisher.publish(new MoneyDepositedEvent(account.getId(), money, account.getBalance()));

        return AccountInfo.fromDomain(account);
    }
}
