package com.github.wallet.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AccountId Value Object")
class AccountIdTest {

    @Test
    @DisplayName("should generate unique IDs")
    void shouldGenerateUniqueIds() {
        final AccountId id1 = AccountId.generate();
        final AccountId id2 = AccountId.generate();

        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotEqualTo(id2.value());
    }

    @Test
    @DisplayName("should create from string")
    void shouldCreateFromString() {
        final AccountId id = AccountId.of("test-id-123");

        assertThat(id.value()).isEqualTo("test-id-123");
        assertThat(id.toString()).isEqualTo("test-id-123");
    }

    @Test
    @DisplayName("should throw on null value")
    void shouldThrowOnNullValue() {
        assertThatThrownBy(() -> AccountId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw on blank value")
    void shouldThrowOnBlankValue() {
        assertThatThrownBy(() -> AccountId.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    @DisplayName("should be equal when values match")
    void shouldBeEqualWhenValuesMatch() {
        final AccountId id1 = AccountId.of("same-id");
        final AccountId id2 = AccountId.of("same-id");

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
