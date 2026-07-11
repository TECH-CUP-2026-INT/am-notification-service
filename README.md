# Servicio de Notificaciones — TechCup Fútbol

Microservicio del sistema TechCup Fútbol (torneo universitario). Es puramente un
**consumidor de eventos** de otros microservicios y un **productor de alertas in-app**
para el usuario final: nunca genera notificaciones por iniciativa propia, siempre
reacciona a algo que ocurrió en otro servicio.

## Arquitectura

Capas: `controller` → `listener` (puerto de entrada) → `service` → `repository`.

- **`controller/events/*`**: webhooks REST que exponen un endpoint por evento de
  origen. Solo deserializan y validan el payload HTTP.
- **`listener/*`**: interpretan el evento de dominio (sanción, mensaje, invitación...)
  y lo traducen a un `CreateNotificationCommand`. No conocen el repositorio ni el
  modelo de persistencia.
- **`service/NotificationServiceImpl`**: no sabe de dónde vino la notificación; solo
  persiste y resuelve las consultas del historial (campanita).
- **`security/*`**: dos mecanismos de autenticación conviven en la misma cadena de
  filtros: `JwtClaimsFilter` decodifica el JWT que ya validó el API Gateway (para los
  endpoints de usuario) e `InternalApiKeyFilter` valida una API key compartida (para
  los webhooks de eventos servicio-a-servicio).

Esta separación permite reemplazar el transporte (REST → cola de eventos, por
ejemplo) sin tocar la lógica de negocio: solo cambiaría el `controller`.

## Decisiones de integración

- **Mecanismo**: REST síncrono, igual que el resto de microservicios del equipo (ver
  `service-match/integration/*`). No hay cola de eventos en la plataforma hoy; introducir
  una para un solo servicio habría exigido cambios en 5 equipos dueños de los eventos,
  fuera del alcance de este repo.
- **Seguridad de los webhooks**: header `X-Internal-Api-Key` (configurable vía
  `INTERNAL_API_KEY`), porque esos endpoints reciben POSTs servicio-a-servicio sin JWT
  de usuario.
- **Persistencia**: PostgreSQL + Flyway (`db/migration/V1__init_schema.sql`), con
  `ddl-auto: validate` (el esquema lo gobierna la migración, no Hibernate).

## Modelo de datos

Tabla `notification`: `id`, `recipient_id` (destinatario), `type` (tipo de evento,
enum explícito — pensado para accesibilidad, no solo color/ícono), `message`, `reference_id`
(id del recurso relacionado, para que el frontend navegue), `is_read`, `created_at`, `read_at`.

`NotificationType` cubre los 8 requerimientos funcionales con 13 valores (se separan
aprobada/rechazada/cancelada y programado/reprogramado/cancelado en constantes
distintas, no un tipo genérico + campo de estado, para que el tipo por sí solo sea
semánticamente inequívoco para un lector de pantalla).

## Endpoints

### Consultados por el frontend (JWT del Gateway)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/notificaciones?leidas={true\|false}` | Historial del usuario autenticado (filtro opcional) |
| GET | `/api/notificaciones/no-leidas/conteo` | Conteo para el ícono de campanita |
| PATCH | `/api/notificaciones/{id}/leer` | Marca una notificación como leída |
| PATCH | `/api/notificaciones/leer-todas` | Marca todas como leídas |

### Webhooks de eventos (API key interna)

| Origen | Endpoint | Estado del contrato |
|---|---|---|
| Servicio de Partidos | `POST /api/notificaciones/sanciones` | ✅ **Confirmado** — coincide con `RestSanctionNotifier` de `service-match` |
| Servicio de Comunicaciones | `POST /api/notificaciones/mensajes` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/solicitudes` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/respuestas` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/invitaciones` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/estado` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/comprobante` | ⚠️ Propuesto |
| Servicio de Agendamiento | `POST /api/notificaciones/partidos` | ⚠️ Propuesto |

Los payloads propuestos están documentados en el Javadoc de cada clase en
`dto/event/*`, junto con las preguntas abiertas para el equipo dueño (por ejemplo: si
`recipientId` siempre es un único usuario o si el origen debe hacer fan-out por cada
destinatario). El equipo de Partidos además deberá agregar el header
`X-Internal-Api-Key` a `RestSanctionNotifier`, que hoy no lo envía.

## Configuración

Variables de entorno (con default para desarrollo local):

| Variable | Default |
|---|---|
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5432` / `techcup_notifications` / `postgres` / `postgres` |
| `SERVER_PORT` | `8083` (coincide con el `NOTIFICACIONES_SERVICE_URL` por defecto que ya usa `service-match`) |
| `INTERNAL_API_KEY` | `local-dev-internal-key` |

## Cómo correrlo

### Con Docker (recomendado para probarlo o mostrárselo al equipo)

```bash
docker compose up --build
```

Levanta Postgres (con healthcheck) y la app ya conectada a él. Flyway aplica la
migración automáticamente al arrancar. Cuando ambos contenedores estén arriba:

- Swagger UI: http://localhost:8083/swagger-ui/index.html
- Health: http://localhost:8083/actuator/health
- Postgres queda expuesto en `localhost:5433` (no `5432`, para no chocar con otro
  Postgres que ya tengas corriendo local) por si quieres conectarte con un cliente.

`docker compose down` para apagar todo; agrega `-v` si además quieres borrar los
datos persistidos.

### Guía completa de pruebas en Swagger

Abre http://localhost:8083/swagger-ui/index.html. Vas a ver dos grupos de endpoints:
los webhooks de eventos (`sanciones`, `mensajes`, `equipos`, `inscripciones`,
`partidos`) y los de usuario (`notificaciones`). Cada uno pide un esquema de
autenticación distinto, y ambos están declarados en Swagger, así que todo se prueba
desde el botón **Authorize** (candado arriba a la derecha) sin tocar Postman.

#### 1. Autorizarte

En el modal de **Authorize** vas a ver dos candados:

- **`internalApiKey`**: pega `local-dev-internal-key` (es el valor por defecto de
  `INTERNAL_API_KEY` en `docker-compose.yml`). Con esto quedan habilitados los 8
  webhooks de eventos.
- **`bearerAuth`**: como este servicio confía en que el API Gateway ya validó la firma
  (`JwtClaimsFilter` solo lee el claim `sub`, no la reverifica), **no necesitas un JWT
  real firmado** para probar en local. Genera uno con forma válida:

  ```bash
  ./scripts/generate-test-jwt.sh 33333333-3333-3333-3333-333333333333
  ```

  Pega el resultado (empieza con `eyJ...` y termina en un punto) en `bearerAuth`. Ese
  UUID (`3333...`) va a ser tu "usuario de pruebas": todo lo que dispares con ese id
  como destinatario aparecerá cuando consultes con este mismo JWT.

Dale **Authorize** en ambos y cierra el modal — ya puedes probar cualquier endpoint
desde **Try it out** sin repetir headers.

#### 2. Disparar los 8 eventos

Prueba cada uno con **Try it out** en su endpoint, usando siempre
`33333333-3333-3333-3333-333333333333` como destinatario para que todo se acumule en
el mismo historial. Todos deben responder `202 Accepted`.

**`POST /api/notificaciones/sanciones`** (contrato ✅ confirmado con Partidos)
```json
{
  "matchId": "11111111-1111-1111-1111-111111111111",
  "teamId": "22222222-2222-2222-2222-222222222222",
  "playerId": "33333333-3333-3333-3333-333333333333",
  "triggeringCardType": "YELLOW",
  "yellowCardsInMatch": 2,
  "occurredAt": "2026-07-11T20:00:00Z"
}
```

**`POST /api/notificaciones/mensajes`**
```json
{
  "chatId": "a1111111-1111-1111-1111-111111111111",
  "senderId": "a2222222-2222-2222-2222-222222222222",
  "senderName": "Ana",
  "recipientId": "33333333-3333-3333-3333-333333333333",
  "messagePreview": "Nos vemos en la cancha",
  "sentAt": "2026-07-11T20:00:00Z"
}
```

**`POST /api/notificaciones/equipos/solicitudes`**
```json
{
  "teamId": "b1111111-1111-1111-1111-111111111111",
  "teamName": "Halcones FC",
  "requesterId": "b2222222-2222-2222-2222-222222222222",
  "requesterName": "Camilo",
  "recipientId": "33333333-3333-3333-3333-333333333333",
  "requestId": "b3333333-3333-3333-3333-333333333333",
  "occurredAt": "2026-07-11T20:00:00Z"
}
```

**`POST /api/notificaciones/equipos/respuestas`**
```json
{
  "teamId": "b1111111-1111-1111-1111-111111111111",
  "teamName": "Halcones FC",
  "requestId": "b3333333-3333-3333-3333-333333333333",
  "recipientId": "33333333-3333-3333-3333-333333333333",
  "accepted": true,
  "respondedAt": "2026-07-11T20:05:00Z"
}
```

**`POST /api/notificaciones/equipos/invitaciones`**
```json
{
  "teamId": "c1111111-1111-1111-1111-111111111111",
  "teamName": "Aguilas United",
  "invitedUserId": "33333333-3333-3333-3333-333333333333",
  "invitationId": "c2222222-2222-2222-2222-222222222222",
  "invitedBy": "Laura",
  "occurredAt": "2026-07-11T20:00:00Z"
}
```

**`POST /api/notificaciones/inscripciones/estado`**
```json
{
  "enrollmentId": "d1111111-1111-1111-1111-111111111111",
  "teamId": "b1111111-1111-1111-1111-111111111111",
  "recipientId": "33333333-3333-3333-3333-333333333333",
  "newStatus": "APROBADA",
  "reason": null,
  "occurredAt": "2026-07-11T20:00:00Z"
}
```

**`POST /api/notificaciones/inscripciones/comprobante`**
```json
{
  "enrollmentId": "d2222222-2222-2222-2222-222222222222",
  "teamId": "b1111111-1111-1111-1111-111111111111",
  "recipientId": "33333333-3333-3333-3333-333333333333",
  "proofUrl": "https://storage.techcup.co/comprobantes/d2222222.pdf",
  "receivedAt": "2026-07-11T20:00:00Z"
}
```

**`POST /api/notificaciones/partidos`**
```json
{
  "matchId": "e1111111-1111-1111-1111-111111111111",
  "teamHomeId": "b1111111-1111-1111-1111-111111111111",
  "teamAwayId": "c1111111-1111-1111-1111-111111111111",
  "recipientId": "33333333-3333-3333-3333-333333333333",
  "action": "PROGRAMADO",
  "scheduledAt": "2026-08-01T20:00:00Z",
  "previousScheduledAt": null,
  "occurredAt": "2026-07-11T20:00:00Z"
}
```

#### 3. Consultar como el usuario final

Con el `bearerAuth` ya puesto:

- **`GET /api/notificaciones`** → deberías ver las 8 notificaciones que acabas de
  crear, la más reciente primero. Prueba también con `?leidas=false`.
- **`GET /api/notificaciones/no-leidas/conteo`** → `{"count": 8}`.
- **`PATCH /api/notificaciones/{id}/leer`** → copia un `id` de la respuesta anterior,
  pégalo en el path param y ejecútalo; el campo `read` debe pasar a `true` y `readAt`
  se llena.
- **`PATCH /api/notificaciones/leer-todas`** → marca el resto; después,
  `no-leidas/conteo` debe dar `{"count": 0}`.

#### 4. Confirmar que la seguridad está bloqueando lo que debe

- Quita la API key del Authorize y repite un `POST` de evento → `403`.
- Deja solo el JWT (sin API key) y prueba un `POST` de evento → también `403` (el JWT
  no sirve para autenticar webhooks).
- Deja solo la API key (sin JWT) y prueba un `GET /api/notificaciones` → `401`, porque
  `CurrentUserProvider` exige específicamente un principal de tipo usuario, no de
  servicio interno.

Ya validé este flujo completo end-to-end (los 8 eventos + los 4 endpoints de usuario +
los tres casos de seguridad) contra el stack de Docker antes de escribir esta guía.

### Sin Docker

```bash
./mvnw spring-boot:run
```

Requiere una instancia de PostgreSQL accesible por tu cuenta (Flyway aplica la
migración al arrancar); usa las variables de entorno de la sección anterior para
apuntarlo a tu base.

## Pruebas

```bash
./mvnw test
```

Cubre la lógica de negocio clave: construcción de la notificación a partir de cada
tipo de evento (un test por listener) y las reglas de `NotificationService`
(pertenencia del destinatario, idempotencia de "marcar como leída", conteo de no
leídas). El test de contexto completo (`ServiceNotificationsApplicationTests`)
requiere una PostgreSQL real disponible, igual que en `service-match`.
