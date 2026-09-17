package com.github.wallet.domain.model;

import com.github.wallet.application.dto.TransferRequest;
import com.github.wallet.application.usecase.CreateAccountUseCase;
import com.github.wallet.application.usecase.DepositMoneyUseCase;
import com.github.wallet.application.usecase.TransferMoneyUseCase;
import com.github.wallet.domain.service.FraudDetectionService;
import com.github.wallet.infrastructure.event.SimpleEventPublisher;
import com.github.wallet.infrastructure.persistence.InMemoryAccountRepository;
import com.github.wallet.infrastructure.persistence.InMemoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Transfer Invariant Property-Based Tests")
class TransferInvariantIT {

    private InMemoryAccountRepository accountRepository;
    private InMemoryTransactionRepository transactionRepository;
    private SimpleEventPublisher eventPublisher;
    private FraudDetectionService fraudDetectionService;

    private CreateAccountUseCase createAccount;
    private DepositMoneyUseCase deposit;
    private TransferMoneyUseCase transfer;

    @BeforeEach
    void setUp() {
        accountRepository = new InMemoryAccountRepository();
        transactionRepository = new InMemoryTransactionRepository();
        eventPublisher = new SimpleEventPublisher();
        fraudDetectionService = new FraudDetectionService(Money.of(BigDecimal.valueOf(1000000), Currency.USD));

        createAccount = new CreateAccountUseCase(accountRepository, eventPublisher);
        deposit = new DepositMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
        transfer = new TransferMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
    }

    @Test
    @DisplayName("transfer should preserve total balance across accounts")
    void transferShouldPreserveTotalBalance() {
        createAccount.execute("Alice", Currency.USD);
        createAccount.execute("Bob", Currency.USD);

        deposit.execute("Alice", BigDecimal.valueOf(1000));
        deposit.execute("Bob", BigDecimal.valueOf(500));

        final BigDecimal totalBefore = getTotalBalance();

        transfer.execute(new TransferRequest("Alice", "Bob", BigDecimal.valueOf(300)));

        final BigDecimal totalAfter = getTotalBalance();

        assertThat(totalAfter).isEqualByComparingTo(totalBefore);
    }

    @RepeatedTest(value = 20, name = "random transfer #{currentRepetition} preserves total")
    @DisplayName("random transfers should always preserve total balance")
    void randomTransfersShouldPreserveTotalBalance() {
        final Random random = new Random();

        createAccount.execute("Sender", Currency.USD);
        createAccount.execute("Receiver", Currency.USD);

        final BigDecimal initialDeposit = BigDecimal.valueOf(10000);
        deposit.execute("Sender", initialDeposit);

        final BigDecimal totalBefore = getTotalBalance();

        // Random transfer amount between 1 and 5000
        final BigDecimal transferAmount = BigDecimal.valueOf(random.nextInt(5000) + 1);
        transfer.execute(new TransferRequest("Sender", "Receiver", transferAmount));

        final BigDecimal totalAfter = getTotalBalance();

        assertThat(totalAfter).isEqualByComparingTo(totalBefore);
    }

    @RepeatedTest(value = 10, name = "sequential transfers #{currentRepetition} preserve total")
    @DisplayName("multiple sequential transfers should preserve total balance")
    void multipleTransfersShouldPreserveTotalBalance() {
        final Random random = new Random();

        createAccount.execute("A", Currency.USD);
        createAccount.execute("B", Currency.USD);
        createAccount.execute("C", Currency.USD);

        deposit.execute("A", BigDecimal.valueOf(5000));
        deposit.execute("B", BigDecimal.valueOf(3000));
        deposit.execute("C", BigDecimal.valueOf(2000));

        final BigDecimal totalBefore = getTotalBalance();

        // Perform random transfers
        final String[] names = {"A", "B", "C"};
        for (int i = 0; i < 5; i++) {
            final int fromIdx = random.nextInt(3);
            int toIdx = random.nextInt(3);
            while (toIdx == fromIdx) {
                toIdx = random.nextInt(3);
            }
            final BigDecimal amount = BigDecimal.valueOf(random.nextInt(100) + 1);
            try {
                transfer.execute(new TransferRequest(names[fromIdx], names[toIdx], amount));
            } catch (final Exception ignored) {
                // Some transfers may fail due to insufficient funds — that's OK
            }
        }

        final BigDecimal totalAfter = getTotalBalance();

        assertThat(totalAfter).isEqualByComparingTo(totalBefore);
    }

    private BigDecimal getTotalBalance() {
        return accountRepository.findAll().stream()
                .map(account -> account.getBalance().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
