package co.edu.escuelaing.techcup.notifications.email;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.escuelaing.techcup.notifications.config.EmailProperties;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConfiguredRecipientEmailResolverTest {

    @Test
    void resolveEmail_withConfiguredTestRecipient_returnsIt() {
        EmailProperties properties = new EmailProperties("no-reply@techcup.com", "dev@techcup.com");
        ConfiguredRecipientEmailResolver resolver = new ConfiguredRecipientEmailResolver(properties);

        Optional<String> result = resolver.resolveEmail(UUID.randomUUID());

        assertThat(result).contains("dev@techcup.com");
    }

    @Test
    void resolveEmail_withoutConfiguredTestRecipient_returnsEmpty() {
        EmailProperties properties = new EmailProperties("no-reply@techcup.com", null);
        ConfiguredRecipientEmailResolver resolver = new ConfiguredRecipientEmailResolver(properties);

        assertThat(resolver.resolveEmail(UUID.randomUUID())).isEmpty();
    }

    @Test
    void resolveEmail_withBlankTestRecipient_returnsEmpty() {
        EmailProperties properties = new EmailProperties("no-reply@techcup.com", "   ");
        ConfiguredRecipientEmailResolver resolver = new ConfiguredRecipientEmailResolver(properties);

        assertThat(resolver.resolveEmail(UUID.randomUUID())).isEmpty();
    }
}
