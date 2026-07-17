# Service Integration

This service never fires a notification on its own initiative — every one
comes from an event reported by another service, either as a REST webhook
or through the shared RabbitMQ broker. It connects to six producing
services and one shared message bus:

- **`am-matches-service`** (astromerge) — card sanctions, confirmed and
  verified end-to-end, plus match-result events consumed from RabbitMQ.
- **`mk-tournament-service`** — conduct sanctions, confirmed, plus
  tournament-finalized events consumed from RabbitMQ (currently logged
  only).
- **Communications Service** — chat messages, contract proposed.
- **Teams Service** — linking requests/responses, invitations, and
  captaincy transfers, contract proposed.
- **Enrollment Service** — status changes, contract proposed. (The
  proof-of-payment integration was discontinued — see
  [Requirements](requerimientos.md).)
- **Scheduling / Tournaments Service** — match scheduling, contract
  proposed.

This page is the source of truth for which of these is actually connected
to a real producer today and which is still waiting on another team to
confirm its contract.

## Status of inbound integrations

| Origin | Endpoint | Status |
|---|---|---|
| **Matches Service** (`am-matches-service`, astromerge) | `POST /api/notificaciones/sanciones` | ✅ **Confirmed and verified end-to-end** — `RestSanctionNotifier` in matches-service sends the `X-Internal-Api-Key` header on every call |
| **Tournaments Service** (`mk-tournament-service`) | `POST /api/notificaciones/sanciones-conducta` | ✅ **Confirmed** — covers conduct sanctions (`SanctionType.CONDUCT`): the Organizer decides, after the fact, how many matches to suspend a player for; unlike `PlayerSanctionedEvent`, it isn't tied to a live match. Tournaments' `SanctionNotificationAdapter` (Feign) only calls this endpoint for `CONDUCT` — automatic sanctions (`RED_CARD`, `YELLOW_CARD_ACCUMULATION`) are still notified exclusively by the Matches Service, so the player isn't notified twice |
| Communications Service | `POST /api/notificaciones/mensajes` | ⚠️ Proposed — contract defined on this side, pending confirmation and producer implementation by the owning team |
| Teams Service | `POST /api/notificaciones/equipos/solicitudes` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/respuestas` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/invitaciones` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/capitania` | ⚠️ Proposed |
| Enrollment Service | `POST /api/notificaciones/inscripciones/estado` | ⚠️ Proposed |
| Scheduling / Tournaments Service | `POST /api/notificaciones/partidos` | ⚠️ Proposed |

The proposed payloads are documented in the Javadoc of each record in
`dto/event/*`, along with the open questions for the owning team (for
example: whether `recipientId` is always a single user or whether the
origin should fan out per recipient).

**Scope note:** none of the "⚠️ Proposed" items are a gap in *this*
repository — the endpoint, the validation, and the translation into a
notification are already implemented and tested on this side. What's
missing is an external service (outside this team's own repos) that calls
that endpoint. There is nothing else this team can do to "close" those
integrations without access to those other services' code.

## RabbitMQ: shared CloudAMQP broker

Beyond webhooks, this service also **consumes** two event types directly
from `techcup.exchange`, the topic exchange shared across the TechCup
platform on a managed [CloudAMQP](https://www.cloudamqp.com/) broker — the
same broker Tournaments and the Statistics service already use. This
service only reads from it; it doesn't publish anything.

| Queue | Routing key | Event | Status |
|---|---|---|---|
| `techcup.notifications.match-events` | `techcup.match.event.*` | `MatchStatEvent` (Competition) | ✅ Confirmed by the Statistics team — exact final routing-key segment still pending, which is why the binding uses the `*` wildcard |
| `techcup.notifications.tournament-events` | `techcup.tournament.event.*` | `TournamentFinalizedEvent` (Tournaments) | ✅ Confirmed — same situation on the routing key; the payload itself doesn't carry enough information to produce a notification yet (see [Architecture](arquitectura.md#why-rabbitmq)) |

The broker's host, username, and virtual host are configured directly in
`application.yml` (this platform's shared broker is not treated as a
secret by itself); only the password (`RABBITMQ_PASS`) is injected as an
environment variable / CI secret, never committed. See
[Configuration](configuracion.md) for the full variable reference.

`RabbitAdmin` is configured to ignore declaration failures on startup, so
if the broker is unreachable (wrong credential, CloudAMQP downtime), the
application still starts and serves the REST API and webhooks normally —
only the RabbitMQ consumption is affected. See
[Architecture](arquitectura.md#why-rabbitmq) for the reasoning.

## Astromerge's own services (D3) and their ports

| Service | App port (Docker) | MongoDB port (host) |
|---|---|---|
| `am-matches-service` | `8080` | `27017` |
| `am-notification-service` | `8083` | `27019` |
| `am-logistic-service` | `8085` | `27018` |

## End-to-end connectivity verification

`am-matches-service` and `am-notification-service` were started together
(`docker compose up --build` in each repo, no port collisions) and a real
sanction was fired from `am-matches-service` (2 yellow cards for the same
player). The call reached `POST /api/notificaciones/sanciones`
authenticated with `X-Internal-Api-Key`, and the notification became
visible when querying `GET /api/notificaciones` authenticated as the
sanctioned player — confirming the integration live, not just via tests.
