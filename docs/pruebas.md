# Pruebas

## Cómo ejecutar las pruebas

```bash
# Suite completa (el test de contexto de Spring levanta MongoDB vía Testcontainers)
./mvnw test

# Suite completa + gate de cobertura (JaCoCo >= 80%)
./mvnw verify
```

El test de contexto completo usa Testcontainers, así que solo necesitas Docker
disponible en la máquina donde corres las pruebas (no hace falta levantar Mongo
a mano). Si igual quieres tener la base arriba para probar manualmente:

```bash
docker compose up -d mongo
```

## Qué cubren las pruebas

| Área | Cubre |
|---|---|
| `listener/*ListenerImplTest` | Construcción de la notificación a partir de cada tipo de evento (sanción, mensaje, invitación, etc.) — un test por listener |
| `service/NotificationServiceImplTest` | Reglas de `NotificationService`: pertenencia del destinatario, idempotencia de "marcar como leída", conteo de no leídas |
| `controller/*` | Contrato HTTP de los endpoints de usuario y de los webhooks de eventos |
| `security/*` | `JwtClaimsFilter`, `InternalApiKeyFilter`, `CurrentUserProvider` (incluyendo que un principal de servicio interno no pueda leer el historial de usuario, y viceversa) |
| `exception/*` | `GlobalExceptionHandler` y forma del `ErrorResponse` |
| `ServiceNotificationsApplicationTests` | Carga del contexto de Spring Boot (MongoDB real vía Testcontainers) |

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
`README.md`): los 8 webhooks de eventos, los 4 endpoints de usuario, y los
tres casos de seguridad (sin API key, sin JWT, mecanismo cruzado).

Ver [Configuración](configuracion.md) para variables de entorno y
[Arquitectura](arquitectura.md) para el detalle de las reglas de negocio que
estas pruebas verifican.
