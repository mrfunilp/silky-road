package com.silkyroad.backend.user;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silkyroad.backend.common.TooManyRequestsException;
import org.junit.jupiter.api.Test;

class LoginRateLimiterTest {

    private final LoginRateLimiter limiter = new LoginRateLimiter();

    @Test
    void locksUsernameAfterFiveFailures() {
        for (int i = 0; i < 5; i++) {
            limiter.checkAllowed("alice", "1.1.1.1");
            limiter.recordFailure("Alice","1.1.1." + i);
        }

        assertThatThrownBy(() -> limiter.checkAllowed("ALICE", "2.2.2.2"))
                .isInstanceOf(TooManyRequestsException.class);
        assertThatCode(() -> limiter.checkAllowed("bob", "2.2.2.2")).doesNotThrowAnyException();
    }

    @Test
    void locksIpAfterTwentyFailures() {
        for (int i = 0; i < 20; i++) {
            limiter.recordFailure("user" + i, "1.1.1.1");
        }

        assertThatThrownBy(() -> limiter.checkAllowed("someone-new", "1.1.1.1"))
                .isInstanceOf(TooManyRequestsException.class);
        assertThatCode(() -> limiter.checkAllowed("someone-new", "2.2.2.2")).doesNotThrowAnyException();
    }

    @Test
    void successResetsUsernameCounter() {
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("alice", "1.1.1.1");
        }
        limiter.recordSuccess("alice");
        limiter.recordFailure("alice", "1.1.1.1");

        assertThatCode(() -> limiter.checkAllowed("alice", "1.1.1.1")).doesNotThrowAnyException();
    }
}
