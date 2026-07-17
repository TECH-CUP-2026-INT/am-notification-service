# TechCup Fútbol — Notifications Service

## Notifications Service

Spring Boot microservice of **Astro Merge**, the platform behind the **TechCup
Fútbol** university tournament. It centralizes the end user's in-app alerts
(bell-style): it receives events from other microservices as REST webhooks,
consumes a couple of event types directly from the platform's shared RabbitMQ
broker, and turns each one into a persisted notification — plus a best-effort
email.

[View on GitHub](https://github.com/TECH-CUP-2026-INT/am-notification-service){ .md-button .md-button--primary }
[Explore the API](api.md){ .md-button }

### Description

`service-notifications` (repository `am-notification-service`) is the
microservice responsible for **notifications** within Astro Merge. It is
built in **Java 21** and **Spring Boot 3.5.x**, persists data in **MongoDB**,
and exposes a REST API for both the frontend (notification history) and
other microservices (event webhooks). It also subscribes to the shared
`techcup.exchange` on CloudAMQP to pick up match and tournament events that
don't have a dedicated webhook, and fires an asynchronous email for every
notification created.

### Goal

Give the end user a reliable, accessible notification bell, while keeping
the originating service unaffected by anything that happens on this side:
webhooks respond `202 Accepted` immediately, and every event is translated
into a persisted notification with an explicit `NotificationType`.

### Technologies used

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Programming language |
| Spring Boot | 3.5.6 | Application framework |
| Spring Web | — | REST API (end user and webhooks) |
| Spring Data MongoDB | — | Document-oriented persistence |
| MongoDB | — | Database (compatible with Azure Cosmos DB for MongoDB vCore) |
| Spring Security | — | Gateway JWT (user), internal API key (webhooks), and cookie-based CSRF |
| Spring AMQP | — | Consumes match/tournament events from the shared CloudAMQP broker |
| Spring Mail | — | Best-effort email for every notification created |
| Micrometer + Prometheus + Zipkin | — | Metrics and distributed tracing (observability) |
| springdoc-openapi | 2.7.0 | OpenAPI/Swagger UI for the REST API |
| Lombok | — | Boilerplate code reduction |
| Maven | Wrapper (`mvnw`) | Dependency management and build |
| Testcontainers | — | Real MongoDB in integration tests |
| JaCoCo | 0.8.12 | Test coverage gate (≥ 80%) in CI |
| Docker + Docker Compose | — | Containerization and local orchestration (app + MongoDB + observability stack) |

### Main features

- **Event intake** from other microservices (card and conduct sanctions,
  chat messages, team requests/invitations/captaincy, enrollment status,
  match scheduling) through **9 REST webhooks**.
- **Match-result and tournament notifications** consumed directly from the
  shared RabbitMQ exchange, without a webhook on either side.
- **Notification history** for the authenticated user, filterable by
  read/unread, with individual or bulk mark-as-read.
- **Best-effort email** for every notification, sent asynchronously so a
  slow or failing mail server never delays the response.
- **Failure isolation**: the originating service is never blocked by this
  service (`202 Accepted` for webhooks, best-effort consumption for
  RabbitMQ).
- **Differentiated authentication** between service-to-service (internal
  API key) and end user (Gateway JWT + CSRF), on the same Spring Security
  filter chain.
- **Observability**: health/info/metrics on Actuator, Prometheus scraping,
  and distributed tracing via Zipkin.

### Repository

[TECH-CUP-2026-INT/am-notification-service](https://github.com/TECH-CUP-2026-INT/am-notification-service)

[![CI (Push)](https://github.com/TECH-CUP-2026-INT/am-notification-service/actions/workflows/ci-push.yml/badge.svg)](https://github.com/TECH-CUP-2026-INT/am-notification-service/actions/workflows/ci-push.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TECH-CUP-2026-INT_am-notification-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=TECH-CUP-2026-INT_am-notification-service)

### Documentation map

| Page | What you'll find there |
|---|---|
| [Introduction](introduccion.md) | Context, purpose and scope of the service |
| [Requirements](requerimientos.md) | Functional and non-functional requirements, and technical prerequisites |
| [Configuration](configuracion.md) | Environment variables, local execution and Docker deployment |
| [Architecture](arquitectura.md) | Layers, design decisions, patterns, components, and request flow |
| [API](api.md) | REST endpoints, authentication, CSRF, and Swagger UI |
| [Service Integration](integracion-servicios.md) | Which services this connects to, and the status of each integration |
| [Testing](pruebas.md) | Testing strategy and how to run it |
| [Team](equipo.md) | Members and roles of the TECH-CUP 2026 INT team |
| [Appendices](anexos.md) | References, bibliography and glossary |

### Where to start?

- New to the project? Start with the [Introduction](introduccion.md).
- Want to understand the design and technical decisions? See
  [Architecture](arquitectura.md).
- Going to consume the API? Jump straight to the [API reference](api.md).
- Working on a sibling microservice that sends events to this service?
  Check [Service Integration](integracion-servicios.md) for the current
  contract and open gaps.

### Quick start

```bash
# Starts MongoDB and the service (indexes are created automatically)
docker compose up --build
```

With the service running, explore the API at
[http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html).

For more detail, see [Configuration](configuracion.md) and [API](api.md).
