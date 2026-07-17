# Testing

## Running the tests

```bash
# Full suite (the Spring context test starts MongoDB via Testcontainers)
./mvnw test

# Full suite + coverage gate (JaCoCo >= 80%)
./mvnw verify
```

The full context test uses Testcontainers, so you only need Docker
available on the machine running the tests (no need to start Mongo by
hand). If you still want the database up for manual testing:

```bash
docker compose up -d mongo
```

## What the tests cover

| Area | Covers |
|---|---|
| `listener/*ListenerImplTest` | Building the notification from each event type (sanction, message, invitation, etc.) — one test per listener |
| `service/NotificationServiceImplTest` | `NotificationService` rules: recipient ownership, idempotency of "mark as read", unread count |
| `controller/*` | HTTP contract of the user endpoints and the event webhooks |
| `security/*` | `JwtClaimsFilter`, `InternalApiKeyFilter`, `CurrentUserProvider` (including that an internal-service principal cannot read a user's history, and vice versa) |
| `exception/*` | `GlobalExceptionHandler` and the `ErrorResponse` shape |
| `ServiceNotificationsApplicationTests` | Spring Boot context load (real MongoDB via Testcontainers) |

## Minimum coverage

The CI pipeline enforces an **80%** line-coverage gate with JaCoCo
(`jacoco-maven-plugin`, `check` goal, bound to the `verify` phase),
excluding DTOs, MongoDB entities, configuration classes, and the main
bootstrap class.

## Tests in the CI pipeline

The GitHub Actions workflow (`.github/workflows/ci-push.yml` and the
equivalent `pr-master.yml`/`pr-qa.yml`, mirrored from the already-proven
`am-matches-service` pipeline) starts a PostgreSQL container as a workflow
service, runs `./mvnw test`, publishes the Surefire report, runs `./mvnw
jacoco:check` for the coverage gate, publishes the JaCoCo report, and only
if all of that passes runs static analysis with SonarQube and packages the
JAR.

## Manual end-to-end verification

Beyond the automated tests, the full flow was manually validated against
the Docker stack (see the step-by-step guide in `README.md`): the 8 event
webhooks, the 4 user endpoints, and the three security cases (no API key,
no JWT, cross-mechanism authentication).

See [Configuration](configuracion.md) for environment variables and
[Architecture](arquitectura.md) for the business rules these tests verify.
