package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.TransferRequest;
import com.github.wallet.domain.event.DomainEvent;
import com.github.wallet.domain.event.TransferCompletedEvent;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.exception.CurrencyMismatchException;
import com.github.wallet.domain.exception.InsufficientFundsException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferMoneyUseCase")
class TransferMoneyUseCaseTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private FraudDetectionService fraudDetectionService;

    private TransferMoneyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TransferMoneyUseCase(accountRepository, transactionRepository, eventPublisher, fraudDetectionService);
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("should transfer between accounts with same currency")
        void shouldTransferBetweenAccounts() {
            final Account alice = Account.create("Alice", Currency.USD);
            alice.deposit(Money.of(1000, Currency.USD));
            final Account bob = Account.create("Bob", Currency.USD);

            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(alice));
            when(accountRepository.findByOwnerName("Bob")).thenReturn(Optional.of(bob));

            useCase.execute(new TransferRequest("Alice", "Bob", BigDecimal.valueOf(300)));

            assertThat(alice.getBalance().amount()).isEqualByComparingTo("700.00");
            assertThat(bob.getBalance().amount()).isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("should save both accounts after transfer")
        void shouldSaveBothAccounts() {
            final Account alice = Account.create("Alice", Currency.USD);
            alice.deposit(Money.of(1000, Currency.USD));
            final Account bob = Account.create("Bob", Currency.USD);

            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(alice));
            when(accountRepository.findByOwnerName("Bob")).thenReturn(Optional.of(bob));

            useCase.execute(new TransferRequest("Alice", "Bob", BigDecimal.valueOf(100)));

            verify(accountRepository, times(2)).save(any(Account.class));
        }

        @Test
        @DisplayName("should record two transactions (debit and credit)")
        void shouldRecordTwoTransactions() {
            final Account alice = Account.create("Alice", Currency.USD);
            alice.deposit(Money.of(1000, Currency.USD));
            final Account bob = Account.create("Bob", Currency.USD);

            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(alice));
            when(accountRepository.findByOwnerName("Bob")).thenReturn(Optional.of(bob));

            useCase.execute(new TransferRequest("Alice", "Bob", BigDecimal.valueOf(200)));

            verify(transactionRepository, times(2)).save(any());
        }

        @Test
        @DisplayName("should publish TransferCompletedEvent")
        void shouldPublishTransferCompletedEvent() {
            final Account alice = Account.create("Alice", Currency.USD);
            alice.deposit(Money.of(1000, Currency.USD));
            final Account bob = Account.create("Bob", Currency.USD);

            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(alice));
            when(accountRepository.findByOwnerName("Bob")).thenReturn(Optional.of(bob));

            useCase.execute(new TransferRequest("Alice", "Bob", BigDecimal.valueOf(150)));

            final ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(TransferCompletedEvent.class);
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw when source not found")
        void shouldThrowWhenSourceNotFound() {
            when(accountRepository.findByOwnerName("Ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(new TransferRequest("Ghost", "Bob", BigDecimal.valueOf(100))))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when target not found")
        void shouldThrowWhenTargetNotFound() {
            final Account alice = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(alice));
            when(accountRepository.findByOwnerName("Ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(new TransferRequest("Alice", "Ghost", BigDecimal.valueOf(100))))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("should throw on currency mismatch")
        void shouldThrowOnCurrencyMismatch() {
            final Account alice = Account.create("Alice", Currency.USD);
            alice.deposit(Money.of(1000, Currency.USD));
            final Account bob = Account.create("Bob", Currency.EUR);

            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(alice));
            when(accountRepository.findByOwnerName("Bob")).thenReturn(Optional.of(bob));

            assertThatThrownBy(() -> useCase.execute(new TransferRequest("Alice", "Bob", BigDecimal.valueOf(100))))
                    .isInstanceOf(CurrencyMismatchException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw on insufficient funds")
        void shouldThrowOnInsufficientFunds() {
            final Account alice = Account.create("Alice", Currency.USD);
            alice.deposit(Money.of(50, Currency.USD));
            final Account bob = Account.create("Bob", Currency.USD);

            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(alice));
            when(accountRepository.findByOwnerName("Bob")).thenReturn(Optional.of(bob));

            assertThatThrownBy(() -> useCase.execute(new TransferRequest("Alice", "Bob", BigDecimal.valueOf(100))))
                    .isInstanceOf(InsufficientFundsException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw on transfer to same account")
        void shouldThrowOnTransferToSameAccount() {
            assertThatThrownBy(() -> new TransferRequest("Alice", "Alice", BigDecimal.valueOf(100)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("same account");
        }
    }
}
