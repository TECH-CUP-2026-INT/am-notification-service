# API

The service exposes **two interfaces** with distinct purposes:

- **REST API** — the notification history queried by the frontend, and the
  webhooks that other microservices call to report an event.
- **RabbitMQ messaging** — the equivalent entry point for the same 9
  events, one queue per event type, for producers that publish instead of
  calling a webhook.

The REST API is documented with **springdoc-openapi**: once the app is
running, the interactive Swagger UI is available at
[http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
(raw spec at `/v3/api-docs`). Every endpoint below is described exactly as
it appears there — same summaries, same request/response examples — just
grouped by resource and written out in plain language. RabbitMQ has no
Swagger equivalent; see [Messaging (RabbitMQ)](#messaging-rabbitmq) below
and [Architecture](arquitectura.md#inter-service-communication-api-events)
for the queue/exchange layout.

Every REST endpoint requires either a `Bearer` JWT (end-user endpoints) or
the internal API key (webhooks) — there is no public, unauthenticated
endpoint in this service, since none of its data is meant to be readable
without knowing whose notifications they are.

## Authentication

This service exposes **two** distinct security schemes, both available
from Swagger's **Authorize** button:

- **`internalApiKey`** (header `X-Internal-Api-Key`): for the 9
  service-to-service event webhooks.
- **`bearerAuth`** (header `Authorization: Bearer <jwt>`): for the
  endpoints queried by the end user. The JWT doesn't need a valid signature
  in local development (this service does not re-verify it, that's the
  Gateway's responsibility) — generate one with a valid shape using
  `./scripts/generate-test-jwt.sh <uuid>`.

See [Configuration](configuracion.md) for the complete guide on how to
authorize in Swagger.

## Notification history (end user, `bearerAuth`)

The four endpoints behind the notification bell. All of them resolve the
current user from the JWT's `sub` claim, so a user can only ever see or
mark their own notifications.

| Method | Path | Description |
|---|---|---|
| GET | `/api/notificaciones?leidas={true\|false}` | Returns the authenticated user's history, newest first. The `leidas` filter is optional — omit it to get everything, or pass `true`/`false` to get only read or only unread notifications. |
| GET | `/api/notificaciones/no-leidas/conteo` | Returns just the unread count, for rendering the badge on the bell icon without fetching the whole list. |
| PATCH | `/api/notificaciones/{id}/leer` | Marks a single notification as read. Fails with `404` if it doesn't exist, and with `403` if it belongs to a different user. Idempotent — marking an already-read notification again just returns it unchanged. |
| PATCH | `/api/notificaciones/leer-todas` | Marks every unread notification belonging to the authenticated user as read, in one call. |

## Event webhooks (service-to-service, `internalApiKey`)

One `POST` endpoint per event type. Each one validates its payload with
Bean Validation, translates it into a notification, and responds
`202 Accepted` immediately — the notification is created independently of
that response, so a slow write to MongoDB never makes the caller wait.

| Origin | Endpoint | Contract status |
|---|---|---|
| Matches Service (`am-matches-service`) | `POST /api/notificaciones/sanciones` | ✅ Confirmed |
| Communications Service | `POST /api/notificaciones/mensajes` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/solicitudes` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/respuestas` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/invitaciones` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/capitania` | ⚠️ Proposed |
| Enrollment Service | `POST /api/notificaciones/inscripciones/estado` | ⚠️ Proposed |
| Enrollment Service | `POST /api/notificaciones/inscripciones/comprobante` | ⚠️ Proposed |
| Scheduling Service | `POST /api/notificaciones/partidos` | ⚠️ Proposed |

See [Service Integration](integracion-servicios.md#status-of-inbound-integrations)
for which of these are actually being called by a real producer today.

**`POST /api/notificaciones/sanciones`** — a player was sanctioned for
accumulating cards (or received a direct red card) in a match. Carries
`matchId`, `teamId`, `playerId`, `triggeringCardType`
(`YELLOW`/`RED`), `yellowCardsInMatch`, and `occurredAt`; notifies the
sanctioned player.

**`POST /api/notificaciones/mensajes`** — a new chat message was sent.
Carries `chatId`, `senderId`, `senderName`, `recipientId`, a
`messagePreview` (truncated, not the full message), and `sentAt`; notifies
`recipientId`.

**`POST /api/notificaciones/equipos/solicitudes`** — a player requested to
join a team. Carries `teamId`, `teamName`, `requesterId`, `requesterName`,
`recipientId` (the team's Captain), `requestId`, and `occurredAt`.

**`POST /api/notificaciones/equipos/respuestas`** — the Captain responded
to a linking request. Carries `teamId`, `teamName`, `requestId`,
`recipientId` (the requesting player), a boolean `accepted`, and
`respondedAt`.

**`POST /api/notificaciones/equipos/invitaciones`** — a player was invited
to join a team. Carries `teamId`, `teamName`, `invitedUserId`,
`invitationId`, `invitedBy` (who sent the invitation), and `occurredAt`;
notifies `invitedUserId`.

**`POST /api/notificaciones/equipos/capitania`** — the team captaincy is
being transferred. See the [example below](#example-captaincy-transfer-webhook-proposed-contract)
for how `initiatedBy` determines the recipient.

**`POST /api/notificaciones/inscripciones/estado`** — a team's enrollment
status changed. Carries `enrollmentId`, `teamId`, `recipientId` (the
Captain), an optional `reason`, and `occurredAt`.

**`POST /api/notificaciones/inscripciones/comprobante`** — an enrollment's
proof of payment was received. Carries `enrollmentId`, `teamId`,
`recipientId` (the Organizer), a `proofUrl`, and `receivedAt`.

**`POST /api/notificaciones/partidos`** — a match was scheduled,
rescheduled, or cancelled. Carries `matchId`, `teamHomeId`, `teamAwayId`,
`recipientId`, `scheduledAt`, an optional `previousScheduledAt` (present
on reschedules), and `occurredAt`.

## Example: sanction webhook (confirmed contract)

`POST /api/notificaciones/sanciones`

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

Responds `202 Accepted` with no body.

## Example: captaincy transfer webhook (proposed contract)

`POST /api/notificaciones/equipos/capitania`

```json
{
  "teamId": "22222222-2222-2222-2222-222222222222",
  "teamName": "Los Tigres",
  "currentCaptainId": "33333333-3333-3333-3333-333333333333",
  "newCaptainId": "44444444-4444-4444-4444-444444444444",
  "initiatedBy": "DELEGATION",
  "occurredAt": "2026-07-12T20:00:00Z"
}
```

`initiatedBy` determines the recipient: `DELEGATION` notifies
`newCaptainId` (the current Captain delegated the role); `APPLICATION`
notifies `currentCaptainId` (a player applied to become Captain). Responds
`202 Accepted` with no body.

## Example: querying the history

`GET /api/notificaciones?leidas=false`

Response `200 OK` (list of `NotificationResponse`):

```json
[
  {
    "id": "1f2e3d4c-5b6a-4978-8a9b-0c1d2e3f4a5b",
    "type": "SANCION_TARJETAS",
    "message": "Fuiste sancionado por acumulación de tarjetas amarillas.",
    "referenceId": "11111111-1111-1111-1111-111111111111",
    "read": false,
    "createdAt": "2026-07-11T20:00:01Z",
    "readAt": null
  }
]
```

## Errors

Business errors (notification not found, access denied to another user's
notification) are handled centrally in
`infrastructure/in/rest/exception/GlobalExceptionHandler`
(`@RestControllerAdvice`) and returned with the `ErrorResponse` DTO. Both
`error` and `message` are always in Spanish (Spring's `reasonPhrase`,
which comes in English, is not used):

| Code | Cause | `error` |
|---|---|---|
| `400` | Webhook payload validation (Bean Validation) | "Solicitud inválida" |
| `401` | No valid authentication for the endpoint (JWT for user, API key for webhook) | "No autenticado" |
| `403` | Authenticated with the wrong mechanism (e.g. API key on a user endpoint) | "Acceso denegado" |
| `404` | `NotificationNotFoundException` | "No encontrado" |
| `500` | Unexpected error not explicitly mapped | "Error interno" |

Each of the 13 endpoints has its own `@Operation`/`@ApiResponse`/example
annotations, defined in an interface separate from the controller
(`infrastructure/in/rest/swagger/*Api.java`, implemented by the
corresponding controller) — there are no documentation annotations on the
controllers themselves.

## Messaging (RabbitMQ)

The same 9 events from the webhooks table above can also arrive via
RabbitMQ instead — same validation, same domain ports, same resulting
notification, just a different transport. See
[Architecture](arquitectura.md#inter-service-communication-api-events)
for the exchange/queue layout, retry behavior, and dead-letter handling,
and [Service Integration](integracion-servicios.md) for the routing-key
contract with the shared CloudAMQP broker.
