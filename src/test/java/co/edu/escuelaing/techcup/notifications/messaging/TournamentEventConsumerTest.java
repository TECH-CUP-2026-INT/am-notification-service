package co.edu.escuelaing.techcup.notifications.messaging;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TournamentEventConsumerTest {

    @Test
    void onTournamentEvent_doesNotThrow() {
        TournamentEventConsumer consumer = new TournamentEventConsumer();
        TournamentFinalizedEvent event = new TournamentFinalizedEvent("tn1", LocalDateTime.now());

        assertThatCode(() -> consumer.onTournamentEvent(event)).doesNotThrowAnyException();
    }
}
