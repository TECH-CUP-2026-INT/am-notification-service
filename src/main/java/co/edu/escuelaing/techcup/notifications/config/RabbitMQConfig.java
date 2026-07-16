package co.edu.escuelaing.techcup.notifications.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Conectividad al exchange compartido de RabbitMQ ({@code techcup.exchange}, topic;
 * mismo broker que ya usan Torneos y Estadísticas — ver docs/rabbitmq-integration.md del
 * Servicio de Estadísticas). Este servicio (Notificaciones) declara sus propias colas y
 * se bindea a los eventos que ya existen en el bus.
 *
 * <p>Los listeners (ver paquete {@code messaging}) por ahora solo loguean lo que reciben:
 * ni {@code TournamentFinalizedEvent} ni {@code MatchStatEvent} traen un destinatario ni
 * un mensaje — solo {@code tournamentId}/{@code playerId} — así que no hay todavía un
 * criterio para traducirlos a una notificación real. Falta esa definición de producto
 * antes de crear notificaciones a partir de estos eventos.
 */
@Configuration
public class RabbitMQConfig {

    public static final String TECHCUP_EXCHANGE = "techcup.exchange";
    public static final String MATCH_EVENTS_QUEUE = "techcup.notifications.match-events";
    public static final String MATCH_EVENTS_ROUTING_KEY = "techcup.match.event.*";
    public static final String TOURNAMENT_EVENTS_QUEUE = "techcup.notifications.tournament-events";
    public static final String TOURNAMENT_EVENTS_ROUTING_KEY = "techcup.tournament.event.*";

    @Bean
    public TopicExchange techcupExchange() {
        return new TopicExchange(TECHCUP_EXCHANGE);
    }

    @Bean
    public Queue matchEventsQueue() {
        return new Queue(MATCH_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding matchEventsBinding(Queue matchEventsQueue, TopicExchange techcupExchange) {
        return BindingBuilder.bind(matchEventsQueue).to(techcupExchange).with(MATCH_EVENTS_ROUTING_KEY);
    }

    @Bean
    public Queue tournamentEventsQueue() {
        return new Queue(TOURNAMENT_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding tournamentEventsBinding(Queue tournamentEventsQueue, TopicExchange techcupExchange) {
        return BindingBuilder.bind(tournamentEventsQueue).to(techcupExchange).with(TOURNAMENT_EVENTS_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
