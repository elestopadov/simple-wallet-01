package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.TransferRequest;
import com.github.wallet.domain.event.TransferCompletedEvent;
import com.github.wallet.domain.exception.AccountClosedException;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.exception.CurrencyMismatchException;
import com.github.wallet.domain.exception.InsufficientFundsException;
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

import java.util.Objects;

/**
 * Use case: Transfer money between two accounts.
 */
public final class TransferMoneyUseCase {

    private static final Logger logger = LoggerFactory.getLogger(TransferMoneyUseCase.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;
    private final FraudDetectionService fraudDetectionService;

    public TransferMoneyUseCase(final AccountRepository accountRepository,
                                final TransactionRepository transactionRepository,
                                final EventPublisher eventPublisher,
                                final FraudDetectionService fraudDetectionService) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.fraudDetectionService = Objects.requireNonNull(fraudDetectionService);
    }

    /**
     * Transfer money from source to target account.
     *
     * @throws AccountNotFoundException if either account does not exist
     * @throws AccountClosedException if either account is closed
     * @throws CurrencyMismatchException if accounts have different currencies
     * @throws InvalidAmountException if amount is not positive
     * @throws InsufficientFundsException if source has insufficient funds
     */
    public void execute(final TransferRequest request) {
        Objects.requireNonNull(request, "Transfer request must not be null");

        final Account source = accountRepository.findByOwnerName(request.fromOwnerName())
                .orElseThrow(() -> new AccountNotFoundException(request.fromOwnerName()));

        final Account target = accountRepository.findByOwnerName(request.toOwnerName())
                .orElseThrow(() -> new AccountNotFoundException(request.toOwnerName()));

        if (source.isClosed()) {
            throw new AccountClosedException(source.getId());
        }
        if (target.isClosed()) {
            throw new AccountClosedException(target.getId());
        }

        if (source.getCurrency() != target.getCurrency()) {
            throw new CurrencyMismatchException(source.getCurrency(), target.getCurrency());
        }

        final Money money = Money.of(request.amount(), source.getCurrency());
        if (!money.isPositive()) {
            throw new InvalidAmountException(money);
        }

        fraudDetectionService.validateTransaction(money, source);

        if (money.isGreaterThan(source.getBalance())) {
            throw new InsufficientFundsException(source.getBalance(), money);
        }

        // Execute transfer atomically (in-memory, so no real concurrency concerns for demo)
        source.withdraw(money);
        target.deposit(money);

        accountRepository.save(source);
        accountRepository.save(target);

        // Record both sides of the transfer
        final Transaction debit = Transaction.transferDebit(source.getId(), target.getId(), money);
        final Transaction credit = Transaction.transferCredit(source.getId(), target.getId(), money);
        transactionRepository.save(debit);
        transactionRepository.save(credit);

        logger.info("Transferred {} from '{}' to '{}'", money, request.fromOwnerName(), request.toOwnerName());
        eventPublisher.publish(new TransferCompletedEvent(source.getId(), target.getId(), money));
    }
}
