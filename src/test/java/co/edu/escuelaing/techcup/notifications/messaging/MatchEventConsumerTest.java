package co.edu.escuelaing.techcup.notifications.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MatchEventConsumerTest {

    @Test
    void onMatchEvent_doesNotThrow() {
        MatchEventConsumer consumer = new MatchEventConsumer();
        MatchStatEvent event = new MatchStatEvent(
                "p1", "t1", "m1", "tn1", "WON", 2, 1, 0, 3, 90, 1, false, LocalDateTime.now());

        assertThatCode(() -> consumer.onMatchEvent(event)).doesNotThrowAnyException();
    }
}
