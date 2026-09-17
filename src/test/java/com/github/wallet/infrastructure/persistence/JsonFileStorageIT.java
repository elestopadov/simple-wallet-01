package com.github.wallet.infrastructure.persistence;

import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;
import com.github.wallet.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonFileStorage Integration Test")
class JsonFileStorageIT {

    @TempDir
    Path tempDir;

    private JsonFileStorage storage;

    @BeforeEach
    void setUp() {
        storage = new JsonFileStorage(tempDir);
    }

    @Nested
    @DisplayName("Account Persistence")
    class AccountPersistence {

        @Test
        @DisplayName("should save and load accounts")
        void shouldSaveAndLoadAccounts() {
            final Account alice = Account.create("Alice", Currency.USD);
            alice.deposit(Money.of(1000, Currency.USD));
            final Account bob = Account.create("Bob", Currency.EUR);
            bob.deposit(Money.of(500, Currency.EUR));

            storage.saveAccounts(List.of(alice, bob));
            final List<Account> loaded = storage.loadAccounts();

            assertThat(loaded).hasSize(2);
            assertThat(loaded.get(0).getOwnerName()).isEqualTo("Alice");
            assertThat(loaded.get(0).getBalance().amount()).isEqualByComparingTo("1000.00");
            assertThat(loaded.get(0).getCurrency()).isEqualTo(Currency.USD);
            assertThat(loaded.get(1).getOwnerName()).isEqualTo("Bob");
            assertThat(loaded.get(1).getCurrency()).isEqualTo(Currency.EUR);
        }

        @Test
        @DisplayName("should return empty list when file does not exist")
        void shouldReturnEmptyListWhenFileDoesNotExist() {
            final List<Account> loaded = storage.loadAccounts();

            assertThat(loaded).isEmpty();
        }

        @Test
        @DisplayName("should preserve account IDs through save/load cycle")
        void shouldPreserveAccountIds() {
            final Account account = Account.create("Charlie", Currency.GBP);
            final String originalId = account.getId().value();

            storage.saveAccounts(List.of(account));
            final List<Account> loaded = storage.loadAccounts();

            assertThat(loaded.get(0).getId().value()).isEqualTo(originalId);
        }

        @Test
        @DisplayName("should handle empty list save")
        void shouldHandleEmptyListSave() {
            storage.saveAccounts(List.of());
            final List<Account> loaded = storage.loadAccounts();

            assertThat(loaded).isEmpty();
        }
    }

    @Nested
    @DisplayName("Transaction Persistence")
    class TransactionPersistence {

        @Test
        @DisplayName("should save and load transactions")
        void shouldSaveAndLoadTransactions() {
            final Account alice = Account.create("Alice", Currency.USD);
            final Transaction deposit = Transaction.deposit(alice.getId(), Money.of(500, Currency.USD));
            final Transaction withdrawal = Transaction.withdrawal(alice.getId(), Money.of(200, Currency.USD));

            storage.saveTransactions(List.of(deposit, withdrawal));
            final List<Transaction> loaded = storage.loadTransactions();

            assertThat(loaded).hasSize(2);
            assertThat(loaded.get(0).id().value()).isEqualTo(deposit.id().value());
            assertThat(loaded.get(0).amount().amount()).isEqualByComparingTo("500.00");
            assertThat(loaded.get(1).amount().amount()).isEqualByComparingTo("200.00");
        }

        @Test
        @DisplayName("should preserve transfer transactions with target account")
        void shouldPreserveTransferTransactions() {
            final Account alice = Account.create("Alice", Currency.USD);
            final Account bob = Account.create("Bob", Currency.USD);
            final Transaction transfer = Transaction.transferDebit(alice.getId(), bob.getId(), Money.of(300, Currency.USD));

            storage.saveTransactions(List.of(transfer));
            final List<Transaction> loaded = storage.loadTransactions();

            assertThat(loaded.get(0).targetAccountId()).isNotNull();
            assertThat(loaded.get(0).targetAccountId().value()).isEqualTo(bob.getId().value());
        }

        @Test
        @DisplayName("should return empty list when file does not exist")
        void shouldReturnEmptyListWhenFileDoesNotExist() {
            final List<Transaction> loaded = storage.loadTransactions();

            assertThat(loaded).isEmpty();
        }
    }
}
