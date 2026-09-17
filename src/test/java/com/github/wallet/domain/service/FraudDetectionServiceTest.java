package com.github.wallet.domain.service;

import com.github.wallet.domain.exception.FraudDetectedException;
import com.github.wallet.domain.model.Account;
import com.github.wallet.domain.model.Currency;
import com.github.wallet.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FraudDetectionService")
class FraudDetectionServiceTest {

    private static final Money LIMIT = Money.of(BigDecimal.valueOf(5000), Currency.USD);
    private FraudDetectionService service;
    private Account account;

    @BeforeEach
    void setUp() {
        service = new FraudDetectionService(LIMIT);
        account = Account.create("TestUser", Currency.USD);
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("should create with valid limit")
        void shouldCreateWithValidLimit() {
            assertThat(service.getSingleTransactionLimit()).isEqualTo(LIMIT);
        }

        @Test
        @DisplayName("should throw on null limit")
        void shouldThrowOnNullLimit() {
            assertThatThrownBy(() -> new FraudDetectionService(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw on non-positive limit")
        void shouldThrowOnNonPositiveLimit() {
            assertThatThrownBy(() -> new FraudDetectionService(Money.zero(Currency.USD)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }

    @Nested
    @DisplayName("Transaction Validation")
    class TransactionValidation {

        @ParameterizedTest(name = "amount {0} should pass validation (under limit)")
        @ValueSource(doubles = {0.01, 100, 1000, 4999.99, 5000.00})
        @DisplayName("should allow transactions at or below limit")
        void shouldAllowTransactionsAtOrBelowLimit(double amount) {
            final Money money = Money.of(amount, Currency.USD);

            assertThatCode(() -> service.validateTransaction(money, account))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest(name = "amount {0} should fail validation (over limit)")
        @ValueSource(doubles = {5000.01, 6000, 10000, 99999.99})
        @DisplayName("should reject transactions above limit")
        void shouldRejectTransactionsAboveLimit(double amount) {
            final Money money = Money.of(amount, Currency.USD);

            assertThatThrownBy(() -> service.validateTransaction(money, account))
                    .isInstanceOf(FraudDetectedException.class)
                    .hasMessageContaining("fraud")
                    .hasMessageContaining("limit");
        }

        @Test
        @DisplayName("should throw on null amount")
        void shouldThrowOnNullAmount() {
            assertThatThrownBy(() -> service.validateTransaction(null, account))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw on null account")
        void shouldThrowOnNullAccount() {
            assertThatThrownBy(() -> service.validateTransaction(Money.of(100, Currency.USD), null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
