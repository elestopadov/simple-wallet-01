package com.github.wallet.application.usecase;

import com.github.wallet.application.dto.AccountInfo;
import com.github.wallet.domain.event.AccountCreatedEvent;
import com.github.wallet.domain.event.DomainEvent;
import com.github.wallet.domain.exception.DuplicateAccountException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.AccountStatus;
import com.github.wallet.domain.model.Currency;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAccountUseCase")
class CreateAccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private EventPublisher eventPublisher;

    private CreateAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateAccountUseCase(accountRepository, eventPublisher);
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("should create account with valid name and currency")
        void shouldCreateAccountWithValidNameAndCurrency() {
            when(accountRepository.existsByOwnerName("Alice")).thenReturn(false);

            final AccountInfo result = useCase.execute("Alice", Currency.USD);

            assertThat(result.ownerName()).isEqualTo("Alice");
            assertThat(result.currency()).isEqualTo(Currency.USD);
            assertThat(result.balance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.status()).isEqualTo(AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("should save account to repository")
        void shouldSaveAccountToRepository() {
            when(accountRepository.existsByOwnerName("Bob")).thenReturn(false);

            useCase.execute("Bob", Currency.EUR);

            final ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(captor.capture());
            assertThat(captor.getValue().getOwnerName()).isEqualTo("Bob");
            assertThat(captor.getValue().getCurrency()).isEqualTo(Currency.EUR);
        }

        @Test
        @DisplayName("should publish AccountCreatedEvent")
        void shouldPublishAccountCreatedEvent() {
            when(accountRepository.existsByOwnerName("Charlie")).thenReturn(false);

            useCase.execute("Charlie", Currency.GBP);

            final ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventPublisher).publish(captor.capture());
            assertThat(captor.getValue()).isInstanceOf(AccountCreatedEvent.class);
            final AccountCreatedEvent event = (AccountCreatedEvent) captor.getValue();
            assertThat(event.ownerName()).isEqualTo("Charlie");
            assertThat(event.currency()).isEqualTo(Currency.GBP);
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw on duplicate account name")
        void shouldThrowOnDuplicateAccountName() {
            when(accountRepository.existsByOwnerName("Alice")).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute("Alice", Currency.USD))
                    .isInstanceOf(DuplicateAccountException.class)
                    .hasMessageContaining("Alice");

            verify(accountRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("should throw on null owner name")
        void shouldThrowOnNullOwnerName() {
            assertThatThrownBy(() -> useCase.execute(null, Currency.USD))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw on null currency")
        void shouldThrowOnNullCurrency() {
            assertThatThrownBy(() -> useCase.execute("Alice", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
