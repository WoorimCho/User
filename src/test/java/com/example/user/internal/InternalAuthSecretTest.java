package com.example.user.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** SECURITY.md N1: the shared secret has no default, so a missing or short one must stop startup. */
class InternalAuthSecretTest {

    private static final String LONG_ENOUGH = "x".repeat(InternalAuth.MIN_SECRET_LENGTH);

    @Test
    void missingOrBlankSecretIsRefused() {
        for (String s : new String[] {null, "", "   "}) {
            assertThatThrownBy(() -> InternalAuth.requireSecret(s))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("INTERNAL_AUTH_SECRET");
        }
    }

    @Test
    void shortSecretIsRefusedWithoutEchoingIt() {
        String shortOne = "x".repeat(InternalAuth.MIN_SECRET_LENGTH - 1);
        assertThatThrownBy(() -> InternalAuth.requireSecret(shortOne))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too short")
                .hasMessageNotContaining(shortOne);
    }

    @Test
    void longEnoughSecretPassesThroughUnchanged() {
        assertThat(InternalAuth.requireSecret(LONG_ENOUGH)).isEqualTo(LONG_ENOUGH);
    }

    @Test
    void enabledFilterRefusesToStartWithoutAGoodSecret() {
        assertThatThrownBy(() -> new UserInternalAuthFilter(true, ""))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new UserInternalAuthFilter(true, "short"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enabledFilterStartsWithAGoodSecret() {
        assertThatCode(() -> new UserInternalAuthFilter(true, LONG_ENOUGH)).doesNotThrowAnyException();
    }

    @Test
    void disabledFilterNeedsNoSecret() {
        assertThatCode(() -> new UserInternalAuthFilter(false, "")).doesNotThrowAnyException();
    }
}
