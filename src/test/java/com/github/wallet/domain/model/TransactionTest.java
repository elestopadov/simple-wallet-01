package com.github.wallet.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Transaction Record")
class TransactionTest {

    private final AccountId aliceId = AccountId.of("alice-id");
    private final AccountId bobId = AccountId.of("bob-id");
    private final Money hundred = Money.of(100, Currency.USD);

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("should create deposit transaction")
        void shouldCreateDepositTransaction() {
            final Transaction tx = Transaction.deposit(aliceId, hundred);

            assertThat(tx.id()).isNotNull();
            assertThat(tx.sourceAccountId()).isEqualTo(aliceId);
            assertThat(tx.targetAccountId()).isNull();
            assertThat(tx.amount()).isEqualTo(hundred);
            assertThat(tx.type()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(tx.timestamp()).isNotNull();
            assertThat(tx.description()).contains("Deposit");
        }

        @Test
        @DisplayName("should create withdrawal transaction")
        void shouldCreateWithdrawalTransaction() {
            final Transaction tx = Transaction.withdrawal(aliceId, hundred);

            assertThat(tx.sourceAccountId()).isEqualTo(aliceId);
            assertThat(tx.targetAccountId()).isNull();
            assertThat(tx.type()).isEqualTo(TransactionType.WITHDRAWAL);
            assertThat(tx.description()).contains("Withdrawal");
        }

        @Test
        @DisplayName("should create transfer debit transaction")
        void shouldCreateTransferDebitTransaction() {
            final Transaction tx = Transaction.transferDebit(aliceId, bobId, hundred);

            assertThat(tx.sourceAccountId()).isEqualTo(aliceId);
            assertThat(tx.targetAccountId()).isEqualTo(bobId);
            assertThat(tx.type()).isEqualTo(TransactionType.TRANSFER_DEBIT);
            assertThat(tx.description()).contains("Transfer to");
        }

        @Test
        @DisplayName("should create transfer credit transaction")
        void shouldCreateTransferCreditTransaction() {
            final Transaction tx = Transaction.transferCredit(aliceId, bobId, hundred);

            assertThat(tx.sourceAccountId()).isEqualTo(bobId);
            assertThat(tx.targetAccountId()).isEqualTo(aliceId);
            assertThat(tx.type()).isEqualTo(TransactionType.TRANSFER_CREDIT);
            assertThat(tx.description()).contains("Transfer from");
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should throw on null source account ID")
        void shouldThrowOnNullSourceAccountId() {
            assertThatThrownBy(() -> Transaction.deposit(null, hundred))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw on null amount")
        void shouldThrowOnNullAmount() {
            assertThatThrownBy(() -> Transaction.deposit(aliceId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Account Involvement")
    class AccountInvolvement {

        @Test
        @DisplayName("should detect involvement as source")
        void shouldDetectInvolvementAsSource() {
            final Transaction tx = Transaction.deposit(aliceId, hundred);

            assertThat(tx.involvesAccount(aliceId)).isTrue();
            assertThat(tx.involvesAccount(bobId)).isFalse();
        }

        @Test
        @DisplayName("should detect involvement as target")
        void shouldDetectInvolvementAsTarget() {
            final Transaction tx = Transaction.transferDebit(aliceId, bobId, hundred);

            assertThat(tx.involvesAccount(aliceId)).isTrue();
            assertThat(tx.involvesAccount(bobId)).isTrue();
        }
    }
}
