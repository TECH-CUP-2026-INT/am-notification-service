# Service Integration

This service is **purely an event consumer** — it never fires a
notification on its own initiative. It connects to six other services in
the Astro Merge platform:

- **`am-matches-service`** (own team, astromerge) — sanctions, confirmed
  and verified end-to-end.
- **Communications Service** — chat messages, contract proposed.
- **Teams Service** — linking requests/responses, invitations, and
  captaincy transfers, contract proposed.
- **Enrollment Service** — status changes and proof-of-payment, contract
  proposed.
- **Scheduling / Tournaments Service** — match scheduling, contract
  proposed.
- **Statistics Service** (outbound) — receives a minimal
  `NotificationCreatedMessage` every time a notification is created.

This page is the source of truth for which of these is actually connected
to a real producer today and which is still waiting on another team to
confirm its contract.

## Status of inbound integrations

| Origin | REST endpoint | RabbitMQ queue | Status |
|---|---|---|---|
| **Matches Service** (`am-matches-service`, owned by astromerge) | `POST /api/notificaciones/sanciones` | `notificaciones.sanciones.q` | ✅ **Confirmed and verified end-to-end (via REST)** — `RestSanctionNotifier` in matches-service sends the `X-Internal-Api-Key` header on every call. The RabbitMQ queue is ready for when that team migrates the transport |
| Communications Service | `POST /api/notificaciones/mensajes` | `notificaciones.mensajes.q` | ⚠️ Proposed — contract defined on this side, pending confirmation and producer implementation by the owning team |
| Teams Service | `POST /api/notificaciones/equipos/solicitudes` | `notificaciones.equipos.solicitudes.q` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/respuestas` | `notificaciones.equipos.respuestas.q` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/invitaciones` | `notificaciones.equipos.invitaciones.q` | ⚠️ Proposed |
| Teams Service | `POST /api/notificaciones/equipos/capitania` | `notificaciones.equipos.capitania.q` | ⚠️ Proposed |
| Enrollment Service | `POST /api/notificaciones/inscripciones/estado` | `notificaciones.inscripciones.estado.q` | ⚠️ Proposed |
| Enrollment Service | `POST /api/notificaciones/inscripciones/comprobante` | `notificaciones.inscripciones.comprobante.q` | ⚠️ Proposed |
| Scheduling / Tournaments Service | `POST /api/notificaciones/partidos` | `notificaciones.partidos.q` | ⚠️ Proposed |

The proposed payloads are documented in the Javadoc of each record in
`domain/model/event/*`, along with the open questions for the owning team
(for example: whether `recipientId` is always a single user or whether the
origin should fan out per recipient).

**Scope note:** none of the "⚠️ Proposed" items are a gap in *this*
repository — the REST endpoint, the RabbitMQ queue, the validation, and the
translation into a notification are already implemented and tested on this
side. What's missing is an external service (outside the three astromerge
repos) that calls that endpoint or publishes to that queue. There is
nothing else this team can do to "close" those integrations without access
to those other services' code.

## RabbitMQ: shared CloudAMQP broker

The TechCup platform uses a single managed RabbitMQ broker on
[CloudAMQP](https://www.cloudamqp.com/) (free plan), shared by all of the
team's own microservices.

| Item | Value |
|---|---|
| Host | ask the team via a private channel (this repo is public) |
| Port | `5671` (AMQP over TLS — mandatory, CloudAMQP does not expose the unencrypted port externally) |
| User | ask the team via a private channel |
| Virtual host | ask the team via a private channel |
| Shared exchange | `techcup.exchange` (topic) |

Neither the host, the user, the vhost, nor the password **live in this
repository**: since it is public, publishing the shared broker's
connection topology here would make brute-force attacks against the
password easier, so all four are requested through the team's private
channel (the password specifically from Juan David Rangel Jiménez) and
injected only as environment variables / deployment-platform secrets. See
[Configuration](configuracion.md#connecting-to-the-shared-cloudamqp-rabbitmq-stagingproduction)
for the full list of variables.

**Routing key contract (partial, pending confirmation):**

| Service | Publishes to | Status |
|---|---|---|
| Competition | `techcup.match.event.*` | ✅ Confirmed by the Statistics team — exact final segment (e.g. `techcup.match.event.sancion`) still pending confirmation, which is why the `notificaciones.sanciones.q` binding uses the `*` wildcard |
| Tournaments | `techcup.tournament.event.*` | ✅ Confirmed — same situation, final segment still pending |
| Notifications (this service) | `techcup.notification.event.created` | ⚠️ Proposed by this service, following the same `techcup.<domain>.event.*` pattern — pending confirmation with Statistics |
| Communications / Teams / Enrollment | no confirmed `techcup.*` prefix yet | ⚠️ Their events still arrive through the local `notificaciones.eventos` exchange (see the table above) until they define their convention |

The full contract details live in `docs/rabbitmq-integration.md` in the
Statistics team's repository (not included in this repository) — if that
document defines an exact final segment for
`techcup.match.event.*`/`techcup.tournament.event.*`, update
`ROUTING_KEY_MATCH_EVENTS`/`ROUTING_KEY_TOURNAMENT_EVENTS` in
`RabbitMqConfig` to stop using the wildcard.

**Deserialization compatibility:** since Competition/Tournaments don't know
about this service's `*MessageV1` classes, `Jackson2JsonMessageConverter`
is configured with `TypePrecedence.INFERRED` (instead of requiring the
message's `__TypeId__` header to match a local class name) — the type is
inferred from the corresponding `@RabbitListener` method's parameter.

**Environments:** locally (`docker compose up`) and in tests
(Testcontainers), this service keeps using its own ephemeral RabbitMQ — it
declares the same topology (including `techcup.exchange`) on that local
broker, so the code and tests don't depend on connectivity to CloudAMQP.
Only the deployed environment points to the real shared broker, via the
`RABBITMQ_HOST`/`RABBITMQ_PORT`/`RABBITMQ_SSL_ENABLED`/etc. environment
variables.

## Astromerge's own services (D3) and their ports

| Service | App port (Docker) | MongoDB port (host) |
|---|---|---|
| `am-matches-service` | `8080` | `27017` |
| `am-notification-service` | `8083` | `27019` |
| `am-logistic-service` | `8085` | `27018` |

`am-notification-service` also exposes RabbitMQ on `5674` (AMQP) and
`15674` (admin panel) on the host, via `docker-compose.yml`.

## End-to-end connectivity verification

All 3 services were started at once (`docker compose up --build` in each
repo, with no port collisions) and a real sanction was fired from
`am-matches-service` (2 yellow cards for the same player). The call
reached `POST /api/notificaciones/sanciones` authenticated with
`X-Internal-Api-Key`, and the notification became visible when querying
`GET /api/notificaciones` authenticated as the sanctioned player. This
confirms live (not just via tests) that: (a) the header fix in
`RestSanctionNotifier` works, and (b) this same audit's security fix
—requiring `ROLE_SERVICIO_INTERNO` on the webhooks— doesn't break the
legitimate integration.
