package co.edu.escuelaing.techcup.notifications.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.domain.model.NotificationType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.CardType;
import co.edu.escuelaing.techcup.notifications.domain.model.event.PlayerSanctionedEvent;
import co.edu.escuelaing.techcup.notifications.domain.ports.in.CreateNotificationCommand;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SanctionNotificationComposerTest {

    private final SanctionNotificationComposer composer = new SanctionNotificationComposer();

    @Test
    void compose_redCard_notifiesPlayerAsRecipientWithMatchAsReference() {
        UUID matchId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        PlayerSanctionedEvent event = new PlayerSanctionedEvent(
                matchId, UUID.randomUUID(), playerId, CardType.RED, 0, Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.recipientId()).isEqualTo(playerId);
        assertThat(command.type()).isEqualTo(NotificationType.SANCION_TARJETAS);
        assertThat(command.referenceId()).isEqualTo(matchId);
        assertThat(command.message()).containsIgnoringCase("roja");
    }

    @Test
    void compose_secondYellowCard_messageMentionsAccumulatedCount() {
        UUID playerId = UUID.randomUUID();
        PlayerSanctionedEvent event = new PlayerSanctionedEvent(
                UUID.randomUUID(), UUID.randomUUID(), playerId, CardType.YELLOW, 2, Instant.now());

        CreateNotificationCommand command = composer.compose(event);

        assertThat(command.type()).isEqualTo(NotificationType.SANCION_TARJETAS);
        assertThat(command.message()).contains("2");
    }
}
