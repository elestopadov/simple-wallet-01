package com.github.wallet.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Account Entity")
class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.create("Alice", Currency.USD);
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create account with valid params")
        void shouldCreateAccountWithValidParams() {
            assertThat(account.getOwnerName()).isEqualTo("Alice");
            assertThat(account.getCurrency()).isEqualTo(Currency.USD);
            assertThat(account.getBalance()).isEqualTo(Money.zero(Currency.USD));
            assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(account.getId()).isNotNull();
            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.isActive()).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("should reject invalid owner names")
        void shouldRejectInvalidOwnerNames(String name) {
            assertThatThrownBy(() -> Account.create(name, Currency.USD))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should reject owner name exceeding 100 chars")
        void shouldRejectLongOwnerName() {
            final String longName = "x".repeat(101);
            assertThatThrownBy(() -> Account.create(longName, Currency.USD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("100 characters");
        }

        @Test
        @DisplayName("should trim owner name")
        void shouldTrimOwnerName() {
            final Account trimmed = Account.create("  Bob  ", Currency.EUR);
            assertThat(trimmed.getOwnerName()).isEqualTo("Bob");
        }
    }

    @Nested
    @DisplayName("Deposit")
    class Deposit {

        @Test
        @DisplayName("should deposit positive amount")
        void shouldDepositPositiveAmount() {
            final Money amount = Money.of(500, Currency.USD);

            account.deposit(amount);

            assertThat(account.getBalance()).isEqualTo(Money.of(500, Currency.USD));
        }

        @Test
        @DisplayName("should accumulate multiple deposits")
        void shouldAccumulateMultipleDeposits() {
            account.deposit(Money.of(100, Currency.USD));
            account.deposit(Money.of(200, Currency.USD));
            account.deposit(Money.of(300, Currency.USD));

            assertThat(account.getBalance().amount()).isEqualByComparingTo("600.00");
        }

        @Test
        @DisplayName("should reject zero deposit")
        void shouldRejectZeroDeposit() {
            assertThatThrownBy(() -> account.deposit(Money.zero(Currency.USD)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("should reject deposit with wrong currency")
        void shouldRejectDepositWithWrongCurrency() {
            assertThatThrownBy(() -> account.deposit(Money.of(100, Currency.EUR)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }

        @Test
        @DisplayName("should reject deposit on closed account")
        void shouldRejectDepositOnClosedAccount() {
            account.close();

            assertThatThrownBy(() -> account.deposit(Money.of(100, Currency.USD)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("should reject null deposit")
        void shouldRejectNullDeposit() {
            assertThatThrownBy(() -> account.deposit(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Withdraw")
    class Withdraw {

        @BeforeEach
        void fundAccount() {
            account.deposit(Money.of(1000, Currency.USD));
        }

        @Test
        @DisplayName("should withdraw valid amount")
        void shouldWithdrawValidAmount() {
            account.withdraw(Money.of(300, Currency.USD));

            assertThat(account.getBalance().amount()).isEqualByComparingTo("700.00");
        }

        @Test
        @DisplayName("should withdraw entire balance")
        void shouldWithdrawEntireBalance() {
            account.withdraw(Money.of(1000, Currency.USD));

            assertThat(account.getBalance().isZero()).isTrue();
        }

        @Test
        @DisplayName("should reject withdrawal exceeding balance")
        void shouldRejectWithdrawalExceedingBalance() {
            assertThatThrownBy(() -> account.withdraw(Money.of(1001, Currency.USD)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient funds");
        }

        @Test
        @DisplayName("should reject withdrawal with wrong currency")
        void shouldRejectWithdrawalWithWrongCurrency() {
            assertThatThrownBy(() -> account.withdraw(Money.of(100, Currency.EUR)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }

        @Test
        @DisplayName("should reject zero withdrawal")
        void shouldRejectZeroWithdrawal() {
            assertThatThrownBy(() -> account.withdraw(Money.zero(Currency.USD)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject withdrawal on closed account")
        void shouldRejectWithdrawalOnClosedAccount() {
            account.withdraw(Money.of(1000, Currency.USD));
            account.close();

            assertThatThrownBy(() -> account.withdraw(Money.of(100, Currency.USD)))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Close")
    class Close {

        @Test
        @DisplayName("should close account with zero balance")
        void shouldCloseAccountWithZeroBalance() {
            account.close();

            assertThat(account.isClosed()).isTrue();
            assertThat(account.isActive()).isFalse();
            assertThat(account.getStatus()).isEqualTo(AccountStatus.CLOSED);
        }

        @Test
        @DisplayName("should reject closing account with non-zero balance")
        void shouldRejectClosingWithNonZeroBalance() {
            account.deposit(Money.of(100, Currency.USD));

            assertThatThrownBy(() -> account.close())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("non-zero balance");
        }

        @Test
        @DisplayName("should reject closing already closed account")
        void shouldRejectClosingAlreadyClosedAccount() {
            account.close();

            assertThatThrownBy(() -> account.close())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("accounts with same ID should be equal")
        void accountsWithSameIdShouldBeEqual() {
            final Account reconstituted = Account.reconstitute(
                    account.getId(), "Alice", Currency.USD,
                    Money.of(500, Currency.USD), AccountStatus.ACTIVE,
                    account.getCreatedAt());

            assertThat(account).isEqualTo(reconstituted);
            assertThat(account.hashCode()).isEqualTo(reconstituted.hashCode());
        }

        @Test
        @DisplayName("accounts with different IDs should not be equal")
        void accountsWithDifferentIdsShouldNotBeEqual() {
            final Account other = Account.create("Alice", Currency.USD);

            assertThat(account).isNotEqualTo(other);
        }
    }

    @Nested
    @DisplayName("Property-based: Deposit + Withdraw invariant")
    class PropertyBased {

        @ParameterizedTest(name = "deposit {0} then withdraw same amount returns to original")
        @MethodSource("com.github.wallet.domain.model.AccountTest#depositWithdrawAmounts")
        @DisplayName("deposit then withdraw should return to original balance")
        void depositThenWithdrawReturnsToOriginal(double amount) {
            final Money original = account.getBalance();
            final Money money = Money.of(amount, Currency.USD);

            account.deposit(money);
            account.withdraw(money);

            assertThat(account.getBalance()).isEqualTo(original);
        }
    }

    static Stream<Double> depositWithdrawAmounts() {
        return Stream.of(0.01, 1.0, 100.0, 999.99, 5000.0, 9999.99);
    }
}
