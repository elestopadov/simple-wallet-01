package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.event.DomainEvent;
import com.github.wallet.domain.event.MoneyDepositedEvent;
import com.github.wallet.domain.exception.AccountClosedException;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.exception.InvalidAmountException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.AccountStatus;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepositMoneyUseCase")
class DepositMoneyUseCaseTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private FraudDetectionService fraudDetectionService;

    private DepositMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DepositMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("should deposit money into existing account")
        void shouldDepositMoneyIntoExistingAccount() {
            final Account account = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            final AccountInfo result = useCase.execute("Alice", BigDecimal.valueOf(500));

            assertThat(result.balance()).isEqualByComparingTo("500.00");
            verify(accountRepository).save(account);
            verify(transactionRepository).save(any());
        }

        @Test
        @DisplayName("should publish MoneyDepositedEvent")
        void shouldPublishMoneyDepositedEvent() {
            final Account account = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            useCase.execute("Alice", BigDecimal.valueOf(250));

            final ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(MoneyDepositedEvent.class);
        }

        @Test
        @DisplayName("should validate transaction with fraud service")
        void shouldValidateWithFraudService() {
            final Account account = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            useCase.execute("Alice", BigDecimal.valueOf(100));

            verify(fraudDetectionService).validateTransaction(any(Money.class), any(Account.class));
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw on non-existing account")
        void shouldThrowOnNonExistingAccount() {
            when(accountRepository.findByOwnerName("Unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("Unknown", BigDecimal.valueOf(100)))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw on closed account")
        void shouldThrowOnClosedAccount() {
            final Account closed = Account.reconstitute(
                    AccountId.of("id"), "Closed", Currency.USD,
                    Money.zero(Currency.USD), AccountStatus.CLOSED, Instant.now());
            when(accountRepository.findByOwnerName("Closed")).thenReturn(Optional.of(closed));

            assertThatThrownBy(() -> useCase.execute("Closed", BigDecimal.valueOf(100)))
                    .isInstanceOf(AccountClosedException.class);
        }

        @Test
        @DisplayName("should throw on zero amount")
        void shouldThrowOnZeroAmount() {
            final Account account = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> useCase.execute("Alice", BigDecimal.ZERO))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        @DisplayName("should throw on negative amount")
        void shouldThrowOnNegativeAmount() {
            final Account account = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> useCase.execute("Alice", BigDecimal.valueOf(-100)))
                    .isInstanceOf(InvalidAmountException.class);
        }
    }
}
