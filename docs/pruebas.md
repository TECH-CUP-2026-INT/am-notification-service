# Pruebas

## Cómo ejecutar las pruebas

```bash
# Suite completa (el test de contexto de Spring y algunos tests de infraestructura
# levantan MongoDB y RabbitMQ vía Testcontainers)
./mvnw test

# Suite completa + gate de cobertura (JaCoCo >= 80%)
./mvnw verify
```

Los tests de contexto e integración usan Testcontainers, así que solo necesitas Docker
disponible en la máquina donde corres las pruebas (no hace falta levantar Mongo/RabbitMQ
a mano). Si igual quieres tener la infraestructura arriba para probar manualmente:

```bash
docker compose up -d mongo rabbitmq
```

## Qué cubren las pruebas

| Área | Cubre |
|---|---|
| `application/usecase/*ListenerImplTest` | Construcción de la notificación a partir de cada tipo de evento de dominio (sanción, mensaje, invitación, etc.) — un test por listener, más propagación de excepciones del caso de uso |
| `application/usecase/NotificationServiceImplTest` | Reglas de `NotificationUseCase`: pertenencia del destinatario, idempotencia de "marcar como leída", conteo de no leídas, validación de `CreateNotificationCommand`, publicación del evento hacia Estadísticas |
| `application/mapper/NotificationMapperTest` | Mapeo dominio → `NotificationResponse` |
| `infrastructure/in/rest/controller/**` | Contrato HTTP de los endpoints de usuario y de los webhooks de eventos, incluyendo validación de payload (body inválido) para cada endpoint |
| `infrastructure/config/security/*` | `JwtClaimsFilter`, `InternalApiKeyFilter`, `CurrentUserProvider` (incluyendo que un principal de servicio interno no pueda leer el historial de usuario, y viceversa) |
| `infrastructure/in/rest/exception/GlobalExceptionHandlerTest` | Los 7 handlers y forma del `ErrorResponse` (mensaje en español consistente) |
| `infrastructure/out/persistence/mongo/NotificationMongoRepositoryTest` | La query custom `markAllAsRead` (`@Query`+`@Update`) contra Mongo real (Testcontainers) |
| `infrastructure/out/persistence/adapter/NotificationRepositoryAdapterTest` | Mapeo dominio ↔ documento en el adaptador de persistencia |
| `infrastructure/out/messaging/producer/NotificationEventPublisherTest` | Payload mínimo publicado hacia el exchange de Estadísticas |
| `infrastructure/out/messaging/consumer/RabbitMqConsumerIntegrationTest` | Un mensaje válido llega al mismo puerto de dominio que el webhook REST equivalente; un mensaje corrupto termina en la DLQ tras los reintentos (Testcontainers RabbitMQ) — representa el patrón común a los 9 consumers |
| `ServiceNotificationsApplicationTests` | Carga del contexto de Spring Boot (MongoDB y RabbitMQ reales vía Testcontainers) |

## Cobertura mínima

El pipeline de CI aplica un gate de cobertura de línea del **80%** con
JaCoCo (`jacoco-maven-plugin`, goal `check`, atado a la fase `verify`),
excluyendo DTOs, entidades de MongoDB, clases de configuración y la clase principal
de arranque.

## Pruebas en el pipeline de CI

El workflow de GitHub Actions (`.github/workflows/ci-push.yml` y los
equivalentes `pr-master.yml`/`pr-qa.yml`, calcados del pipeline ya probado
de `am-matches-service`) levanta un contenedor de PostgreSQL como servicio,
ejecuta `./mvnw test`, publica el reporte de Surefire, corre `./mvnw
jacoco:check` para el gate de cobertura, publica el reporte de JaCoCo, y
solo si todo eso pasa corre el análisis estático con SonarQube y empaqueta
el JAR.

## Verificación end-to-end manual

Además de las pruebas automatizadas, el flujo completo se validó
manualmente contra el stack de Docker (ver la guía paso a paso en el
`README.md`): los 9 webhooks de eventos, los 4 endpoints de usuario, los
tres casos de seguridad (sin API key, sin JWT, mecanismo cruzado), y la
publicación de un mensaje de prueba directamente en `notificaciones.sanciones.q`
vía el panel de RabbitMQ, confirmando que ambos caminos de entrada (REST y
RabbitMQ) producen el mismo resultado.

Ver [Configuración](configuracion.md) para variables de entorno y
[Arquitectura](arquitectura.md) para el detalle de las reglas de negocio que
estas pruebas verifican.
