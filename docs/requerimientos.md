# Requirements

Functional requirements taken from the **astromerge** team's requirements
sheet (domain D3 — Operations and Communication) for the Notifications
Service, contrasted against the current implementation.

## Functional requirements (requirements sheet)

| ID | Requirement | Status |
|---|---|---|
| RF-01 | Sanction notification for accumulated cards, only to the sanctioned player | ✅ Implemented and **confirmed** with the real producer (`am-matches-service`) — `POST /api/notificaciones/sanciones` |
| RF-02 | New chat message notification, to all active members | ⚠️ Endpoint implemented (`POST /api/notificaciones/mensajes`), **proposed** contract — pending confirmation with the Communications Service (another team) |
| RF-03 | Team-linking request notification, to the Captain | ⚠️ Endpoint implemented (`POST /api/notificaciones/equipos/solicitudes`), **proposed** contract — pending from the Teams Service |
| RF-04 | Response-to-linking-request notification, to the requesting player | ⚠️ Endpoint implemented (`POST /api/notificaciones/equipos/respuestas`), **proposed** contract — pending from the Teams Service |
| RF-05 | Team invitation notification, to the invited player | ⚠️ Endpoint implemented (`POST /api/notificaciones/equipos/invitaciones`), **proposed** contract — pending from the Teams Service |
| RF-06 | Enrollment status change notification, to the Captain | ⚠️ Endpoint implemented (`POST /api/notificaciones/inscripciones/estado`), **proposed** contract — pending from the Enrollment Service |
| RF-07 | Enrollment completed with proof-of-payment notification, to the Organizer | ⚠️ Endpoint implemented (`POST /api/notificaciones/inscripciones/comprobante`), **proposed** contract — pending from the Enrollment Service |
| RF-08 | Match scheduling, rescheduling, or cancellation notification, to the Captain and affected players | ⚠️ Endpoint implemented (`POST /api/notificaciones/partidos`), **proposed** contract — pending from the Scheduling/Tournaments Service |
| RF-09 | Team captaincy transfer notification | ✅ Endpoint implemented (`POST /api/notificaciones/equipos/capitania`), **proposed** contract — pending from the Teams Service |
| RF-10 | Notification history query: each user only sees their own, filterable | ✅ Implemented (`GET /api/notificaciones`, `?leidas=`) |

!!! note "Closed functional gaps"
    RF-09 (captaincy transfer) was identified as a gap during a previous
    audit and has already been implemented: `CaptaincyTransferEvent`
    covers both directions of the requirement (delegation by the current
    Captain → notifies the chosen player; application by a player →
    notifies the current Captain) with the `CAPITANIA_CEDIDA` and
    `CAPITANIA_SOLICITADA` types. The remaining "⚠️ proposed" items are not
    gaps in this repository: they are endpoints already built and tested
    on this side, waiting for the team that owns the event (a different
    domain, outside astromerge) to confirm the exact contract and start
    calling them — see the status table in [Architecture](arquitectura.md).

## Non-functional requirements

| ID | Requirement |
|---|---|
| RNF-01 | **Failure isolation**: this service must never block the originating service; webhooks respond `202 Accepted` quickly. |
| RNF-02 | **Network security**: the service does not verify the JWT signature (the Gateway's responsibility), so it must remain unreachable from outside the platform's internal network. |
| RNF-03 | **Differentiated authentication**: event webhooks (service-to-service) and end-user endpoints use distinct, non-interchangeable authentication mechanisms — a user JWT must not be able to authenticate a webhook, nor vice versa. |
| RNF-04 | **Accessibility**: every event type has an explicit, semantically unambiguous `NotificationType` (not a generic type plus color/icon). |
| RNF-05 | **Privacy**: a user can only view and mark as read their own notifications. |
| RNF-06 | **Maintainability**: the transport (REST today) is decoupled from the business logic behind the `listener` layer, so it can be replaced (e.g. by an event queue) without touching `service`. |
| RNF-07 | **Build reproducibility**: the project must compile, test, and package deterministically via the Maven Wrapper, both locally and in CI. |
| RNF-08 | **Test coverage**: at least 80% line coverage over the business logic (excluding DTOs, MongoDB entities, and configuration classes), automatically verified in CI (JaCoCo). |

## Technical prerequisites

To develop and run the service locally:

| Tool | Minimum version | Use |
|---|---|---|
| [Java (JDK)](https://adoptium.net/) | 21 | Compiling and running the service |
| [Docker](https://www.docker.com/) / Docker Compose | 24+ | MongoDB database and application container (also used by Testcontainers in the tests) |
| [Git](https://git-scm.com/) | 2.x | Version control |
| Maven Wrapper (`mvnw`, included in the repo) | — | No local Maven installation required |

To work on the documentation:

| Tool | Minimum version | Use |
|---|---|---|
| [Python](https://www.python.org/) | 3.9+ | Required by MkDocs |
| [MkDocs](https://www.mkdocs.org/) + [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) | — | Generating the documentation site |

See [Configuration](configuracion.md) for the installation steps for each
tool and the service's environment variables.
