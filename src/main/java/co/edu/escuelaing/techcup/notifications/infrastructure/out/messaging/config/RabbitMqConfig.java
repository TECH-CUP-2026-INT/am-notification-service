package co.edu.escuelaing.techcup.notifications.infrastructure.out.messaging.config;

import co.edu.escuelaing.techcup.notifications.infrastructure.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

/**
 * Dos exchanges topic de entrada, cada uno con una cola + una cola muerta (DLQ) por
 * evento (ver infrastructure/in/rest/controller/events para el equivalente REST, que
 * sigue activo en paralelo):
 *
 * <ul>
 *   <li>{@code notificaciones.eventos} (local, propio de este servicio): chat, equipos,
 *       inscripciones — sus productores (Comunicaciones, Equipos, Inscripción) todavía no
 *       tienen un prefijo {@code techcup.*} confirmado en el broker compartido.</li>
 *   <li>{@code techcup.exchange} (compartido en CloudAMQP entre todos los
 *       microservicios de TechCup, ver docs/arquitectura.md#rabbitmq-cloudamqp-compartido):
 *       sanciones y partidos, publicados por Competencia (routing key
 *       {@code techcup.match.event.*}) y Torneos ({@code techcup.tournament.event.*})
 *       respectivamente. Los routing keys exactos (el segmento final tras
 *       {@code event.}) son una estimación pendiente de confirmar con esos equipos —
 *       por eso las bindings usan el comodín {@code *} en vez de una key exacta.</li>
 * </ul>
 *
 * Un mensaje que falla 3 veces (con backoff) se rechaza sin reencolar; RabbitMQ lo
 * enruta automáticamente a su DLQ vía x-dead-letter-exchange (siempre la DLX local,
 * independientemente de en cuál de los dos exchanges de entrada llegó).
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    public static final String QUEUE_SANCIONES = "notificaciones.sanciones.q";
    public static final String QUEUE_MENSAJES = "notificaciones.mensajes.q";
    public static final String QUEUE_EQUIPOS_SOLICITUDES = "notificaciones.equipos.solicitudes.q";
    public static final String QUEUE_EQUIPOS_RESPUESTAS = "notificaciones.equipos.respuestas.q";
    public static final String QUEUE_EQUIPOS_INVITACIONES = "notificaciones.equipos.invitaciones.q";
    public static final String QUEUE_EQUIPOS_CAPITANIA = "notificaciones.equipos.capitania.q";
    public static final String QUEUE_INSCRIPCIONES_ESTADO = "notificaciones.inscripciones.estado.q";
    public static final String QUEUE_INSCRIPCIONES_COMPROBANTE = "notificaciones.inscripciones.comprobante.q";
    public static final String QUEUE_PARTIDOS = "notificaciones.partidos.q";

    /** Routing key publicada por Competencia según docs/rabbitmq-integration.md (repo Estadísticas). */
    public static final String ROUTING_KEY_MATCH_EVENTS = "techcup.match.event.*";
    /** Routing key publicada por Torneos según docs/rabbitmq-integration.md (repo Estadísticas). */
    public static final String ROUTING_KEY_TOURNAMENT_EVENTS = "techcup.tournament.event.*";

    private static final List<EventRoute> LOCAL_EVENT_ROUTES = List.of(
            new EventRoute("mensajes.chat", QUEUE_MENSAJES),
            new EventRoute("equipos.solicitud", QUEUE_EQUIPOS_SOLICITUDES),
            new EventRoute("equipos.respuesta", QUEUE_EQUIPOS_RESPUESTAS),
            new EventRoute("equipos.invitacion", QUEUE_EQUIPOS_INVITACIONES),
            new EventRoute("equipos.capitania", QUEUE_EQUIPOS_CAPITANIA),
            new EventRoute("inscripciones.estado", QUEUE_INSCRIPCIONES_ESTADO),
            new EventRoute("inscripciones.comprobante", QUEUE_INSCRIPCIONES_COMPROBANTE));

    private static final List<EventRoute> SHARED_EVENT_ROUTES = List.of(
            new EventRoute(ROUTING_KEY_MATCH_EVENTS, QUEUE_SANCIONES),
            new EventRoute(ROUTING_KEY_TOURNAMENT_EVENTS, QUEUE_PARTIDOS));

    private final MessagingProperties messagingProperties;

    private record EventRoute(String routingKey, String queueName) {
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(messagingProperties.exchange(), true, false);
    }

    @Bean
    public TopicExchange eventsDeadLetterExchange() {
        return new TopicExchange(messagingProperties.dlxExchange(), true, false);
    }

    @Bean
    public TopicExchange sharedExchange() {
        return new TopicExchange(messagingProperties.sharedExchange(), true, false);
    }

    @Bean
    public Declarables eventQueuesAndBindings() {
        List<Declarable> declarables = new ArrayList<>();
        declareRoutes(declarables, LOCAL_EVENT_ROUTES, eventsExchange());
        declareRoutes(declarables, SHARED_EVENT_ROUTES, sharedExchange());
        return new Declarables(declarables);
    }

    private void declareRoutes(List<Declarable> declarables, List<EventRoute> routes, TopicExchange exchange) {
        for (EventRoute route : routes) {
            String dlqName = route.queueName() + ".dlq";
            Queue mainQueue = QueueBuilder.durable(route.queueName())
                    .withArgument("x-dead-letter-exchange", messagingProperties.dlxExchange())
                    .withArgument("x-dead-letter-routing-key", dlqName)
                    .build();
            Queue deadLetterQueue = QueueBuilder.durable(dlqName).build();

            declarables.add(mainQueue);
            declarables.add(deadLetterQueue);
            declarables.add(BindingBuilder.bind(mainQueue).to(exchange).with(route.routingKey()));
            declarables.add(BindingBuilder.bind(deadLetterQueue).to(eventsDeadLetterExchange()).with(dlqName));
        }
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        // Los productores de otros equipos (Competencia, Torneos) no conocen nuestras
        // clases *MessageV1, así que su header __TypeId__ nunca va a matchear una clase
        // local: se infiere el tipo a partir del parámetro del método @RabbitListener
        // en vez de exigir que el header coincida con un nombre de clase conocido.
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public RetryOperationsInterceptor rabbitRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000L, 2.0, 10000L)
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter,
            RetryOperationsInterceptor rabbitRetryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(rabbitRetryInterceptor);
        return factory;
    }
}
