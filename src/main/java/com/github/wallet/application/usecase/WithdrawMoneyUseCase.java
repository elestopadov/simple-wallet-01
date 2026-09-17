package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.event.MoneyWithdrawnEvent;
import com.github.wallet.domain.exception.AccountClosedException;
import com.github.wallet.domain.exception.AccountNotFoundException;
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

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Use case: Withdraw money from an account.
 */
public final class WithdrawMoneyUseCase {

    private static final Logger logger = LoggerFactory.getLogger(WithdrawMoneyUseCase.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EventPublisher eventPublisher;
    private final FraudDetectionService fraudDetectionService;

    public WithdrawMoneyUseCase(final AccountRepository accountRepository,
                                final TransactionRepository transactionRepository,
                                final EventPublisher eventPublisher,
                                final FraudDetectionService fraudDetectionService) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.fraudDetectionService = Objects.requireNonNull(fraudDetectionService);
    }

    /**
     * Withdraw the specified amount from the named account.
     *
     * @throws AccountNotFoundException if account does not exist
     * @throws AccountClosedException if account is closed
     * @throws InvalidAmountException if amount is not positive
     * @throws InsufficientFundsException if balance is too low
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

        if (money.isGreaterThan(account.getBalance())) {
            throw new InsufficientFundsException(account.getBalance(), money);
        }

        account.withdraw(money);
        accountRepository.save(account);

        final Transaction transaction = Transaction.withdrawal(account.getId(), money);
        transactionRepository.save(transaction);

        logger.info("Withdrawn {} from account '{}'", money, ownerName);
        eventPublisher.publish(new MoneyWithdrawnEvent(account.getId(), money, account.getBalance()));

        return AccountInfo.fromDomain(account);
    }
}
