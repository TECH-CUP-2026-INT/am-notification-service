# API

The service exposes **two interfaces** with distinct purposes:

- **REST API** — the notification history queried by the frontend, and the
  9 webhooks that other microservices call to report an event.
- **RabbitMQ consumer** — a read-only subscription to the platform's
  shared `techcup.exchange`, for the two event types (match results,
  tournament finals) that arrive from the bus instead of a webhook. See
  [Messaging (RabbitMQ)](#messaging-rabbitmq).

The REST API is documented with **springdoc-openapi**: once the app is
running, the interactive Swagger UI is available at
[http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
(raw spec at `/v3/api-docs`). The controllers don't carry manual
`@Operation`/example annotations — what you see in Swagger is generated
straight from the endpoints and their DTOs, so the descriptions below add
the plain-language context Swagger doesn't show on its own.

There is no public, unauthenticated endpoint in this service: every REST
endpoint requires either a `Bearer` JWT (end-user endpoints) or the
internal API key (webhooks). User endpoints that change state (`PATCH`)
additionally require a CSRF token; see [Authentication](#authentication).

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

Since the service is `STATELESS` (no server-side session), state-changing
user endpoints are additionally protected with a cookie-based CSRF token:
the frontend must read the `XSRF-TOKEN` cookie and echo its value in the
`X-XSRF-TOKEN` header on every `PATCH` (both endpoints below). Webhook
paths are exempt from CSRF — they're always called server-to-server, never
from a browser, so there's no session to forge. Swagger's **Try it out**
doesn't add this header automatically, so the two `PATCH` endpoints can't
be exercised from the UI alone; see [Configuration](configuracion.md) for
the full walkthrough.

## Notification history (end user, `bearerAuth`)

The four endpoints behind the notification bell. All of them resolve the
current user from the JWT's `sub` claim, so a user can only ever see or
mark their own notifications.

| Method | Path | Description |
|---|---|---|
| GET | `/api/notificaciones?leidas={true\|false}` | Returns the authenticated user's history, newest first. The `leidas` filter is optional — omit it to get everything, or pass `true`/`false` to get only read or only unread notifications. |
| GET | `/api/notificaciones/no-leidas/conteo` | Returns just the unread count, for rendering the badge on the bell icon without fetching the whole list. |
| PATCH | `/api/notificaciones/{id}/leer` | Marks a single notification as read. Requires the CSRF header. Fails with `404` if it doesn't exist, and with `403` if it belongs to a different user. Idempotent — marking an already-read notification again just returns it unchanged. |
| PATCH | `/api/notificaciones/leer-todas` | Marks every unread notification belonging to the authenticated user as read, in one call. Requires the CSRF header. |

## Event webhooks (service-to-service, `internalApiKey`)

One `POST` endpoint per event type, spread across 6 controllers. Each one
validates its payload with Bean Validation, translates it into a
notification, and responds `202 Accepted` immediately — the notification
(and its email) are created independently of that response.

| Origin | Endpoint | Contract status |
|---|---|---|
| Matches Service (`am-matches-service`) | `POST /api/notificaciones/sanciones` | ✅ Confirmed |
| Tournaments Service (`mk-tournament-service`) | `POST /api/notificaciones/sanciones-conducta` | ✅ Confirmed |
| Communications Service | `POST /api/notificaciones/mensajes` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/solicitudes` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/respuestas` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/invitaciones` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/capitania` | ⚠️ Proposed |
| Enrollment Service | `POST /api/notificaciones/inscripciones/estado` | ⚠️ Proposed |
| Scheduling Service | `POST /api/notificaciones/partidos` | ⚠️ Proposed |

See [Service Integration](integracion-servicios.md#status-of-inbound-integrations)
for which of these are actually being called by a real producer today.

**`POST /api/notificaciones/sanciones`** — a player was sanctioned for
accumulating cards (or received a direct red card) in a live match.
Carries `matchId`, `teamId`, `playerId`, `triggeringCardType`
(`YELLOW`/`RED`), `yellowCardsInMatch`, and `occurredAt`; notifies the
sanctioned player.

**`POST /api/notificaciones/sanciones-conducta`** — the Organizer
suspends a player for a number of matches after reviewing their conduct,
unrelated to a specific live match. Carries `playerId` (a `String`, not a
`UUID` — mirrors how Tournaments models it), `matchesSuspended`, `reason`,
and `occurredAt`.

**`POST /api/notificaciones/mensajes`** — a new chat message was sent.
Carries `chatId`, `senderId`, `senderName`, `recipientId`, a
`messagePreview`, and `sentAt`; notifies `recipientId`.

**`POST /api/notificaciones/equipos/solicitudes`** — a player requested to
join a team. Carries `teamId`, `teamName`, `requesterId`, `requesterName`,
`recipientId` (the team's Captain), `requestId`, and `occurredAt`.

**`POST /api/notificaciones/equipos/respuestas`** — the Captain responded
to a linking request. Carries `teamId`, `teamName`, `requestId`,
`recipientId` (the requesting player), a boolean `accepted`, and
`respondedAt`.

**`POST /api/notificaciones/equipos/invitaciones`** — a player was invited
to join a team. Carries `teamId`, `teamName`, `invitedUserId`,
`invitationId`, `invitedBy`, and `occurredAt`; notifies `invitedUserId`.

**`POST /api/notificaciones/equipos/capitania`** — the team captaincy is
being transferred. See the [example below](#example-captaincy-transfer-webhook-proposed-contract)
for how `initiatedBy` determines the recipient.

**`POST /api/notificaciones/inscripciones/estado`** — a team's enrollment
status changed to `APROBADA`, `RECHAZADA`, or `CANCELADA`. Carries
`enrollmentId`, `teamId`, `recipientId`, `newStatus`, an optional
`reason`, and `occurredAt`.

**`POST /api/notificaciones/partidos`** — a match was scheduled,
rescheduled, or cancelled (`action`: `PROGRAMADO`/`REPROGRAMADO`/
`CANCELADO`). Carries `matchId`, `teamHomeId`, `teamAwayId`,
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

## Example: conduct-sanction webhook (confirmed contract)

`POST /api/notificaciones/sanciones-conducta`

```json
{
  "playerId": "33333333-3333-3333-3333-333333333333",
  "matchesSuspended": 2,
  "reason": "Agresión verbal a un árbitro tras la finalización del partido.",
  "occurredAt": "2026-07-15T20:00:00Z"
}
```

Unlike the card-sanction webhook, this one isn't tied to a live match —
it's fired by the Tournaments Service when the Organizer applies a
conduct sanction, not by the Matches Service. Responds `202 Accepted`
with no body.

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

Business and framework errors are handled centrally in
`exception/GlobalExceptionHandler` (`@RestControllerAdvice`) and returned
with the `ErrorResponse` DTO (`timestamp`, `status`, `error`, `message`,
`path`). `error` is the standard HTTP reason phrase; `message` is a
human-readable detail, in Spanish for the handlers with a custom message
and in whatever language Bean Validation's default messages resolve to for
payload-validation errors:

| Code | Cause |
|---|---|
| `400` | Webhook payload validation (Bean Validation), or a request body that couldn't be parsed |
| `401` | No valid authentication for the endpoint (JWT for user, API key for webhook) |
| `403` | Wrong authentication mechanism, insufficient role, or missing/invalid CSRF token |
| `404` | `NotificationNotFoundException` |
| `500` | Unexpected error not explicitly mapped |

## Messaging (RabbitMQ)

Two event types skip the webhook path entirely and are consumed directly
from the shared `techcup.exchange` on CloudAMQP:

- **`MatchStatEvent`** (`techcup.match.event.*`) — a player's match result
  (won/lost/drawn, goals, cards). Translated into a `RESULTADO_PARTIDO`
  notification the same way a webhook event would be, just without a
  `listener` interface in between.
- **`TournamentFinalizedEvent`** (`techcup.tournament.event.*`) — logged
  only for now; see [Why RabbitMQ?](arquitectura.md#why-rabbitmq) for why
  it doesn't produce a notification yet.

See [Architecture](arquitectura.md#inter-service-communication-api-events)
for the reasoning behind this second channel, and
[Service Integration](integracion-servicios.md) for the routing-key
contract with the shared broker.
