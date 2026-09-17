package com.github.wallet.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object")
class MoneyTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create money with amount and currency")
        void shouldCreateMoneyWithAmountAndCurrency() {
            final Money money = Money.of(BigDecimal.valueOf(100), Currency.USD);

            assertThat(money.amount()).isEqualByComparingTo("100.00");
            assertThat(money.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("should create money from double")
        void shouldCreateMoneyFromDouble() {
            final Money money = Money.of(99.99, Currency.EUR);

            assertThat(money.amount()).isEqualByComparingTo("99.99");
            assertThat(money.currency()).isEqualTo(Currency.EUR);
        }

        @Test
        @DisplayName("should create zero money")
        void shouldCreateZeroMoney() {
            final Money zero = Money.zero(Currency.GBP);

            assertThat(zero.amount()).isEqualByComparingTo("0.00");
            assertThat(zero.isZero()).isTrue();
        }

        @Test
        @DisplayName("should scale amount to 2 decimal places")
        void shouldScaleAmount() {
            final Money money = Money.of(BigDecimal.valueOf(100.999), Currency.USD);

            assertThat(money.amount()).isEqualByComparingTo("101.00");
        }

        @Test
        @DisplayName("should throw on null amount")
        void shouldThrowOnNullAmount() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null, Currency.USD))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw on null currency")
        void shouldThrowOnNullCurrency() {
            assertThatThrownBy(() -> Money.of(BigDecimal.TEN, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Arithmetic")
    class Arithmetic {

        @Test
        @DisplayName("should add two money amounts")
        void shouldAddTwoMoneyAmounts() {
            final Money a = Money.of(100.50, Currency.USD);
            final Money b = Money.of(200.25, Currency.USD);

            final Money result = a.add(b);

            assertThat(result.amount()).isEqualByComparingTo("300.75");
            assertThat(result.currency()).isEqualTo(Currency.USD);
        }

        @Test
        @DisplayName("should subtract two money amounts")
        void shouldSubtractTwoMoneyAmounts() {
            final Money a = Money.of(500.00, Currency.EUR);
            final Money b = Money.of(200.50, Currency.EUR);

            final Money result = a.subtract(b);

            assertThat(result.amount()).isEqualByComparingTo("299.50");
        }

        @Test
        @DisplayName("should throw on currency mismatch in add")
        void shouldThrowOnCurrencyMismatchInAdd() {
            final Money usd = Money.of(100, Currency.USD);
            final Money eur = Money.of(100, Currency.EUR);

            assertThatThrownBy(() -> usd.add(eur))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }

        @Test
        @DisplayName("should throw on currency mismatch in subtract")
        void shouldThrowOnCurrencyMismatchInSubtract() {
            final Money usd = Money.of(100, Currency.USD);
            final Money gbp = Money.of(50, Currency.GBP);

            assertThatThrownBy(() -> usd.subtract(gbp))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Currency mismatch");
        }

        @Test
        @DisplayName("should throw on null in add")
        void shouldThrowOnNullInAdd() {
            final Money money = Money.of(100, Currency.USD);

            assertThatThrownBy(() -> money.add(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Comparisons")
    class Comparisons {

        @ParameterizedTest(name = "amount={0} → isPositive={1}, isNegative={2}, isZero={3}")
        @MethodSource("com.github.wallet.domain.model.MoneyTest#comparisonData")
        @DisplayName("should correctly identify positive/negative/zero")
        void shouldCorrectlyIdentifySign(double amount, boolean positive, boolean negative, boolean zero) {
            final Money money = Money.of(amount, Currency.USD);

            assertThat(money.isPositive()).isEqualTo(positive);
            assertThat(money.isNegative()).isEqualTo(negative);
            assertThat(money.isZero()).isEqualTo(zero);
        }

        @Test
        @DisplayName("should compare greater than correctly")
        void shouldCompareGreaterThanCorrectly() {
            final Money large = Money.of(1000, Currency.USD);
            final Money small = Money.of(100, Currency.USD);

            assertThat(large.isGreaterThan(small)).isTrue();
            assertThat(small.isGreaterThan(large)).isFalse();
            assertThat(large.isGreaterThan(large)).isFalse();
        }

        @Test
        @DisplayName("should compare greater than or equal correctly")
        void shouldCompareGreaterThanOrEqualCorrectly() {
            final Money a = Money.of(100, Currency.EUR);
            final Money b = Money.of(100, Currency.EUR);
            final Money c = Money.of(50, Currency.EUR);

            assertThat(a.isGreaterThanOrEqual(b)).isTrue();
            assertThat(a.isGreaterThanOrEqual(c)).isTrue();
            assertThat(c.isGreaterThanOrEqual(a)).isFalse();
        }
    }

    @Nested
    @DisplayName("Display")
    class Display {

        @Test
        @DisplayName("should format toString correctly")
        void shouldFormatToString() {
            final Money money = Money.of(1234.56, Currency.USD);

            assertThat(money.toString()).isEqualTo("1234.56 USD");
        }

        @Test
        @DisplayName("should format display string with symbol")
        void shouldFormatDisplayString() {
            final Money money = Money.of(1234.56, Currency.USD);

            assertThat(money.toDisplayString()).isEqualTo("$1234.56");
        }
    }

    static Stream<Arguments> comparisonData() {
        return Stream.of(
                Arguments.of(100.0, true, false, false),
                Arguments.of(-50.0, false, true, false),
                Arguments.of(0.0, false, false, true),
                Arguments.of(0.001, false, false, true),  // rounds to 0.00
                Arguments.of(0.005, true, false, false)   // rounds to 0.01
        );
    }
}
