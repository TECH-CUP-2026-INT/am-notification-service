# Arquitectura

## Capas (hexagonal)

```
domain/
├── model/            Notification, NotificationType, model/event/* (records de evento, sin anotaciones de framework)
├── ports/
│   ├── in/           NotificationUseCase, CreateNotificationCommand, *EventListener (6 interfaces)
│   └── out/          NotificationRepositoryPort, NotificationEventPublisherPort
└── exception/        NotificationNotFoundException, NotificationAccessDeniedException

application/
├── usecase/          NotificationServiceImpl, *EventListenerImpl (6) — lógica de negocio, sin conocer HTTP ni Mongo ni RabbitMQ
└── mapper/           NotificationMapper (dominio → DTO de respuesta)

infrastructure/
├── in/rest/
│   ├── controller/          Webhooks REST + API de usuario (delegan en los puertos de entrada, sin lógica de negocio)
│   ├── swagger/              Interfaces con las anotaciones OpenAPI (@Operation/@ApiResponse/ejemplos), separadas del controller
│   └── dto/{request,response}/  DTOs de borde HTTP, con Bean Validation
├── out/
│   ├── persistence/
│   │   ├── mongo/            NotificationDocument (@Document), NotificationMongoRepository (Spring Data)
│   │   ├── mapper/            NotificationPersistenceMapper (dominio ↔ documento)
│   │   └── adapter/           NotificationRepositoryAdapter (implementa el puerto de salida)
│   └── messaging/
│       ├── config/            RabbitMqConfig (exchanges, colas, DLQ, reintentos)
│       ├── dto/                *MessageV1 (payloads de entrada versionados) + NotificationCreatedMessage (salida hacia Estadísticas)
│       ├── consumer/           9 *Consumer (@RabbitListener) — segunda puerta de entrada, mismos puertos de dominio que los controllers
│       └── producer/           NotificationEventPublisher (implementa el puerto de salida hacia Estadísticas)
└── config/            SecurityConfig, OpenApiConfig, InternalApiKeyProperties, MessagingProperties
    └── security/       JwtClaimsFilter, InternalApiKeyFilter, CurrentUserProvider, AuthenticatedUser, InternalServicePrincipal
```

Regla de dependencia: `domain` no importa nada de `application` ni `infrastructure`. `application` solo depende de `domain` (puertos e interfaces). `infrastructure` depende de `domain`/`application` para invocarlos, nunca al revés. Los `record` de evento en `domain/model/event` son el contrato de los puertos de entrada; los DTOs HTTP (`infrastructure/in/rest/dto/request`) y los payloads de RabbitMQ (`infrastructure/out/messaging/dto`) son conversiones de borde (`toDomain()`) hacia ese mismo contrato — así REST y RabbitMQ son dos adaptadores intercambiables sobre el mismo caso de uso.

## Modelo de datos

Colección `notification`: `id`, `recipientId` (destinatario), `type` (tipo de
evento, enum explícito — pensado para accesibilidad, no solo color/ícono),
`message`, `referenceId` (id del recurso relacionado, para que el frontend
navegue), `read`, `createdAt`, `readAt`.

`NotificationType` cubre los requerimientos funcionales con valores
explícitos (se separan aprobada/rechazada/cancelada y
programado/reprogramado/cancelado en constantes distintas, no un tipo
genérico + campo de estado, para que el tipo por sí solo sea semánticamente
inequívoco para un lector de pantalla).

## Seguridad

Dos mecanismos de autenticación conviven en la misma cadena de filtros de
Spring Security (`infrastructure/config/SecurityConfig.java`), en el mismo modelo de
confianza que `am-matches-service` y `am-logistic-service` (el API Gateway
ya validó la firma del JWT; este servicio no la revalida):

- **`infrastructure/config/security/JwtClaimsFilter.java`**: decodifica el claim `sub` del JWT
  para los endpoints consultados por el usuario final
  (`/api/notificaciones/**` de lectura/marcado).
- **`infrastructure/config/security/InternalApiKeyFilter.java`**: valida el header
  `X-Internal-Api-Key` (comparación de tiempo constante con `MessageDigest.isEqual`) para
  los 9 webhooks de eventos servicio-a-servicio, autenticando como
  `InternalServicePrincipal` con `ROLE_SERVICIO_INTERNO`.
- **`infrastructure/config/security/CurrentUserProvider.java`**: exige específicamente un
  principal de tipo `AuthenticatedUser` — un `InternalServicePrincipal`
  (autenticado solo con la API key) **no** puede leer el historial de
  notificaciones, y viceversa un JWT de usuario no autentica un webhook.

**Implicación operativa (no negociable), igual que en los otros dos
servicios propios:** este servicio nunca debe exponerse directo a internet
ni a otros servicios que no sea el API Gateway (para los endpoints de
usuario) y los servicios de origen autorizados (para los webhooks). Debe
protegerse a nivel de red.

## Mensajería (RabbitMQ)

Desde esta ronda de correcciones, además de los 9 webhooks REST existe una **segunda puerta
de entrada equivalente** vía RabbitMQ, ambas invocando exactamente los mismos puertos de
dominio (`domain/ports/in`) — así la lógica de negocio no sabe ni le importa por cuál
transporte llegó el evento.

Hay **dos exchanges de entrada distintos** (ver `RabbitMqConfig`), porque no todos los
equipos productores publican todavía en el broker compartido:

- `notificaciones.eventos` (topic, durable, propio de este servicio): mensajes de chat,
  eventos de equipos e inscripción. Sus productores (Comunicaciones, Equipos, Inscripción)
  aún no tienen un prefijo `techcup.*` confirmado en CloudAMQP, así que sus colas siguen
  aquí hasta que se confirme (ver sección siguiente).
- `techcup.exchange` (topic, durable, **compartido en CloudAMQP** entre todos los
  microservicios de TechCup — ver "RabbitMQ: CloudAMQP compartido" más abajo): sanciones y
  partidos, publicados por Competencia y Torneos respectivamente.
- `notificaciones.eventos.dlx` (topic, durable): dead-letter exchange local, usado para
  las colas de ambos exchanges de entrada — el mecanismo de reintentos/DLQ es un detalle de
  infraestructura propio de este servicio, no necesita ser compartido.

**Salida hacia Estadísticas:** `NotificationEventPublisher` publica un
`NotificationCreatedMessage` (payload mínimo: `notificationId`, `recipientId`, `type`,
`createdAt` — nunca la entidad completa ni el texto del mensaje) al mismo `techcup.exchange`
compartido, con routing key `techcup.notification.event.created`, cada vez que se crea una
notificación.

**Colas de entrada** (una cola + una DLQ por evento, ver `RabbitMqConfig`):

| Exchange | Routing key | Cola | Evento |
|---|---|---|---|
| `techcup.exchange` | `techcup.match.event.*` | `notificaciones.sanciones.q` | Sanción por tarjetas (Competencia) |
| `techcup.exchange` | `techcup.tournament.event.*` | `notificaciones.partidos.q` | Agendamiento de partido (Torneos) |
| `notificaciones.eventos` | `mensajes.chat` | `notificaciones.mensajes.q` | Nuevo mensaje de chat |
| `notificaciones.eventos` | `equipos.solicitud` | `notificaciones.equipos.solicitudes.q` | Solicitud de vinculación a equipo |
| `notificaciones.eventos` | `equipos.respuesta` | `notificaciones.equipos.respuestas.q` | Respuesta a solicitud de vinculación |
| `notificaciones.eventos` | `equipos.invitacion` | `notificaciones.equipos.invitaciones.q` | Invitación a equipo |
| `notificaciones.eventos` | `equipos.capitania` | `notificaciones.equipos.capitania.q` | Cesión/solicitud de capitanía |
| `notificaciones.eventos` | `inscripciones.estado` | `notificaciones.inscripciones.estado.q` | Cambio de estado de inscripción |
| `notificaciones.eventos` | `inscripciones.comprobante` | `notificaciones.inscripciones.comprobante.q` | Comprobante de inscripción recibido |

## RabbitMQ: CloudAMQP compartido

La plataforma TechCup usa un único broker RabbitMQ administrado en
[CloudAMQP](https://www.cloudamqp.com/) (plan free), compartido por todos los
microservicios propios.

| Dato | Valor |
|---|---|
| Host | pedir al equipo por canal privado (este repo es público) |
| Puerto | `5671` (AMQP sobre TLS — obligatorio, CloudAMQP no expone el puerto sin cifrar externamente) |
| Usuario | pedir al equipo por canal privado |
| Virtual host | pedir al equipo por canal privado |
| Exchange compartido | `techcup.exchange` (topic) |

Ni el host, ni el usuario, ni el vhost, ni la contraseña **viven en este repositorio**:
al ser público, publicar aquí la topología de conexión del broker compartido facilita
ataques de fuerza bruta contra la contraseña, así que los cuatro se piden por canal
privado del equipo (la contraseña específicamente a Juan David Rangel Jiménez) y se
inyectan únicamente como variables de entorno / secretos de la plataforma de despliegue.
Ver [Configuración](configuracion.md#conectarse-al-rabbitmq-compartido-de-cloudamqp-stagingproducción)
para la lista completa de variables.

**Contrato de routing keys (parcial, pendiente de confirmar):**

| Servicio | Publica en | Estado |
|---|---|---|
| Competencia | `techcup.match.event.*` | ✅ Confirmado por el equipo de Estadísticas — segmento final exacto (p. ej. `techcup.match.event.sancion`) pendiente de confirmar, por eso la binding de `notificaciones.sanciones.q` usa el comodín `*` |
| Torneos | `techcup.tournament.event.*` | ✅ Confirmado — mismo caso, segmento final pendiente |
| Notificaciones (este servicio) | `techcup.notification.event.created` | ⚠️ Propuesto por este servicio, siguiendo el mismo patrón `techcup.<dominio>.event.*` — pendiente de confirmar con Estadísticas |
| Comunicaciones / Equipos / Inscripción | sin prefijo `techcup.*` confirmado todavía | ⚠️ Sus eventos siguen llegando por el exchange local `notificaciones.eventos` (ver tabla de arriba) hasta que definan su convención |

Los detalles completos del contrato están en `docs/rabbitmq-integration.md` del repo de
Estadísticas (no incluido en este repositorio) — si ese documento define un segmento final
exacto para `techcup.match.event.*`/`techcup.tournament.event.*`, hay que actualizar
`ROUTING_KEY_MATCH_EVENTS`/`ROUTING_KEY_TOURNAMENT_EVENTS` en `RabbitMqConfig` para dejar de
usar el comodín.

**Compatibilidad de deserialización:** como Competencia/Torneos no conocen las clases
`*MessageV1` de este servicio, el `Jackson2JsonMessageConverter` se configura con
`TypePrecedence.INFERRED` (en vez de exigir que el header `__TypeId__` del mensaje coincida
con un nombre de clase local) — el tipo se infiere del parámetro del método
`@RabbitListener` correspondiente.

**Entornos:** en local (`docker compose up`) y en los tests (Testcontainers), este servicio
sigue usando su propio RabbitMQ efímero — declara la misma topología (`techcup.exchange`
incluido) en ese broker local, así que el código y los tests no dependen de conectividad a
CloudAMQP. Solo el entorno desplegado apunta al broker compartido real, vía las variables de
entorno `RABBITMQ_HOST`/`RABBITMQ_PORT`/`RABBITMQ_SSL_ENABLED`/etc.

**Reintentos y DLQ:** cada `@RabbitListener` reintenta hasta 3 veces (backoff exponencial
1s→2s→4s...) vía `RetryOperationsInterceptor`. Si sigue fallando, el contenedor rechaza el
mensaje sin reencolar (`defaultRequeueRejected=false`); como cada cola principal declara
`x-dead-letter-exchange`/`x-dead-letter-routing-key`, RabbitMQ lo enruta automáticamente a
su `<cola>.dlq` para inspección manual, en vez de perderlo o reintentarlo indefinidamente.

**Versionado de contrato:** cada payload de entrada (`infrastructure/out/messaging/dto/*MessageV1`)
incluye `schemaVersion` para poder evolucionar el contrato sin romper consumidores/productores
existentes.

**Plan de migración (REST → RabbitMQ):** los 9 webhooks REST **se mantienen activos** junto
a las colas, porque hoy son el único mecanismo que usan los productores externos
(`RestSanctionNotifier` en `am-matches-service` está confirmado funcionando así) y
retirarlos de golpe rompería esa integración. A medida que cada equipo productor migre a
publicar directamente en la cola correspondiente, su webhook REST puede retirarse sin tocar
`domain` ni `application` — ambos adaptadores invocan el mismo puerto de entrada.

## Estado de las integraciones entrantes

Este servicio es **puramente un consumidor de eventos** — nunca dispara una
notificación por iniciativa propia. La siguiente tabla es la fuente de
verdad de qué integración está realmente conectada con un productor real
propio del equipo (astromerge) y cuál sigue esperando a que otro equipo
confirme su contrato:

| Origen | Endpoint REST | Cola RabbitMQ | Estado |
|---|---|---|---|
| **Servicio de Partidos** (`am-matches-service`, propio de astromerge) | `POST /api/notificaciones/sanciones` | `notificaciones.sanciones.q` | ✅ **Confirmado y verificado end-to-end (vía REST)** — `RestSanctionNotifier` en matches-service envía el header `X-Internal-Api-Key` en cada llamada. La cola RabbitMQ está lista para cuando ese equipo migre el transporte |
| Servicio de Comunicaciones | `POST /api/notificaciones/mensajes` | `notificaciones.mensajes.q` | ⚠️ Propuesto — contrato definido de este lado, pendiente de que el equipo dueño lo confirme e implemente el productor |
| Servicio de Equipos | `POST /api/notificaciones/equipos/solicitudes` | `notificaciones.equipos.solicitudes.q` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/respuestas` | `notificaciones.equipos.respuestas.q` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/invitaciones` | `notificaciones.equipos.invitaciones.q` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/capitania` | `notificaciones.equipos.capitania.q` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/estado` | `notificaciones.inscripciones.estado.q` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/comprobante` | `notificaciones.inscripciones.comprobante.q` | ⚠️ Propuesto |
| Servicio de Agendamiento / Torneos | `POST /api/notificaciones/partidos` | `notificaciones.partidos.q` | ⚠️ Propuesto |

Los payloads propuestos están documentados en el Javadoc de cada record en
`domain/model/event/*`, junto con las preguntas abiertas para el equipo dueño (por
ejemplo: si `recipientId` siempre es un único usuario o si el origen debe
hacer fan-out por cada destinatario).

**Nota de alcance:** ninguno de los ítems "⚠️ Propuesto" es un hueco de
*este* repositorio — el endpoint REST, la cola RabbitMQ, la validación y la traducción a
notificación ya están implementados y probados de este lado. Lo que falta
es un servicio externo (fuera de las tres repos del equipo astromerge) que
llame a ese endpoint o publique en esa cola. No hay nada más que este
equipo pueda hacer para "cerrar" esas integraciones sin acceso al código de esos otros
servicios.

## Servicios propios de astromerge (D3) y sus puertos

| Servicio | Puerto app (Docker) | Puerto MongoDB (host) |
|---|---|---|
| `am-matches-service` | `8080` | `27017` |
| `am-notification-service` | `8083` | `27019` |
| `am-logistic-service` | `8085` | `27018` |

`am-notification-service` además expone RabbitMQ en `5674` (AMQP) y `15674` (panel de
administración) en el host, vía `docker-compose.yml`.

## Verificación de conectividad end-to-end

Se levantaron los 3 servicios a la vez (`docker compose up --build` en cada
repo, sin colisión de puertos) y se disparó una sanción real desde
`am-matches-service` (2 tarjetas amarillas al mismo jugador). La llamada
llegó a `POST /api/notificaciones/sanciones` autenticada con
`X-Internal-Api-Key`, y la notificación quedó visible al consultar `GET
/api/notificaciones` autenticado como el jugador sancionado. Esto confirma
en caliente (no solo con tests) que: (a) el fix del header en
`RestSanctionNotifier` funciona, y (b) el fix de seguridad de esta misma
auditoría —exigir `ROLE_SERVICIO_INTERNO` en los webhooks— no rompe la
integración legítima.
