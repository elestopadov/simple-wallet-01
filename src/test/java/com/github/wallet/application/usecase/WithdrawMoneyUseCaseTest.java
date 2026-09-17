package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.exception.InsufficientFundsException;
import com.github.wallet.domain.exception.InvalidAmountException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;
import com.github.wallet.domain.port.AccountRepository;
import com.github.wallet.domain.port.EventPublisher;
import com.github.wallet.domain.port.TransactionRepository;
import com.github.wallet.domain.service.FraudDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawMoneyUseCase")
class WithdrawMoneyUseCaseTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private FraudDetectionService fraudDetectionService;

    private WithdrawMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new WithdrawMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("should withdraw from funded account")
        void shouldWithdrawFromFundedAccount() {
            final Account account = Account.create("Alice", Currency.USD);
            account.deposit(Money.of(1000, Currency.USD));
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            final AccountInfo result = useCase.execute("Alice", BigDecimal.valueOf(300));

            assertThat(result.balance()).isEqualByComparingTo("700.00");
            verify(accountRepository).save(account);
            verify(transactionRepository).save(any());
            verify(eventPublisher).publish(any());
        }

        @Test
        @DisplayName("should withdraw entire balance")
        void shouldWithdrawEntireBalance() {
            final Account account = Account.create("Alice", Currency.USD);
            account.deposit(Money.of(500, Currency.USD));
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            final AccountInfo result = useCase.execute("Alice", BigDecimal.valueOf(500));

            assertThat(result.balance()).isEqualByComparingTo("0.00");
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw on insufficient funds")
        void shouldThrowOnInsufficientFunds() {
            final Account account = Account.create("Alice", Currency.USD);
            account.deposit(Money.of(100, Currency.USD));
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> useCase.execute("Alice", BigDecimal.valueOf(500)))
                    .isInstanceOf(InsufficientFundsException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw on non-existing account")
        void shouldThrowOnNonExistingAccount() {
            when(accountRepository.findByOwnerName("Ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("Ghost", BigDecimal.valueOf(100)))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("should throw on zero amount")
        void shouldThrowOnZeroAmount() {
            final Account account = Account.create("Alice", Currency.USD);
            account.deposit(Money.of(1000, Currency.USD));
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> useCase.execute("Alice", BigDecimal.ZERO))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }
}
