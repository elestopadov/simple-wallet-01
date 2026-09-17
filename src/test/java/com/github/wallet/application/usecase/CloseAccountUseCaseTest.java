package com.github.wallet.application.usecase;

import com.github.wallet.domain.event.AccountClosedEvent;
import com.github.wallet.domain.event.DomainEvent;
import com.github.wallet.domain.exception.AccountClosedException;
import com.github.wallet.domain.exception.AccountNotFoundException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.AccountId;
import com.github.wallet.domain.model.AccountStatus;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;
import com.github.wallet.domain.port.AccountRepository;
import com.github.wallet.domain.port.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloseAccountUseCase")
class CloseAccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private EventPublisher eventPublisher;

    private CloseAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CloseAccountUseCase(accountRepository, eventPublisher);
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("should close account with zero balance")
        void shouldCloseAccountWithZeroBalance() {
            final Account account = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            useCase.execute("Alice");

            assertThat(account.isClosed()).isTrue();
            verify(accountRepository).save(account);
        }

        @Test
        @DisplayName("should publish AccountClosedEvent")
        void shouldPublishAccountClosedEvent() {
            final Account account = Account.create("Alice", Currency.USD);
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            useCase.execute("Alice");

            final ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(AccountClosedEvent.class);
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw on non-existing account")
        void shouldThrowOnNonExistingAccount() {
            when(accountRepository.findByOwnerName("Ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute("Ghost"))
                    .isInstanceOf(AccountNotFoundException.class);
        }

        @Test
        @DisplayName("should throw on already closed account")
        void shouldThrowOnAlreadyClosedAccount() {
            final Account closed = Account.reconstitute(
                    AccountId.of("id"), "Alice", Currency.USD,
                    Money.zero(Currency.USD), AccountStatus.CLOSED, Instant.now());
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(closed));

            assertThatThrownBy(() -> useCase.execute("Alice"))
                    .isInstanceOf(AccountClosedException.class);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw on account with non-zero balance")
        void shouldThrowOnAccountWithNonZeroBalance() {
            final Account account = Account.create("Alice", Currency.USD);
            account.deposit(Money.of(100, Currency.USD));
            when(accountRepository.findByOwnerName("Alice")).thenReturn(Optional.of(account));

            assertThatThrownBy(() -> useCase.execute("Alice"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("non-zero");
        }
    }
}
