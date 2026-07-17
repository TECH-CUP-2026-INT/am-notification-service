# Configuration

## Clone the repository

```bash
git clone https://github.com/TECH-CUP-2026-INT/am-notification-service.git
cd am-notification-service
```

## Running the service locally

### Option 1: Docker Compose (recommended)

```bash
docker compose up --build
```

Starts MongoDB and RabbitMQ (both with health checks) plus the app already
wired to them. Indexes and queues are created automatically on startup.
Once all three containers are up:

- Swagger UI: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- Health: [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)
- MongoDB is exposed on the host at `27019` (not `27017`, to avoid
  colliding with `am-matches-service`'s Mongo or another local Mongo).
- RabbitMQ (local, for development/tests) is exposed at `5674` (AMQP) and
  `15674` (admin panel, user/password `guest`/`guest`). This local
  RabbitMQ is independent from the shared CloudAMQP broker — see
  [Service Integration](integracion-servicios.md#rabbitmq-shared-cloudamqp-broker).

`docker compose down` shuts everything down; add `-v` if you also want to
delete persisted data.

### Option 2: Local Maven

```bash
./mvnw spring-boot:run
```

Requires a reachable MongoDB instance and a reachable RabbitMQ instance;
use the environment variables below to point it at your infrastructure
(`docker compose up -d mongo rabbitmq` if you only want the infrastructure
and to run the app separately with Maven).

## Environment variables

Nothing about where this service will run — a laptop, a CI runner, Azure
App Service — is hardcoded into the jar. Every value that changes between
those environments (which database to talk to, which broker, which port,
which secret) is read from an environment variable, and `application.yml`
only supplies the **local-development default** for each one, using
Spring's property-placeholder syntax `${VARIABLE_NAME:default-value}`.
That's why `docker compose up` works out of the box with no `.env` file:
every default already points at the containers `docker-compose.yml`
starts, and only deployment environments need to override anything.

Spring resolves these through its standard externalized-configuration
order, so — from lowest to highest precedence — the value in
`application.yml`'s `${...:default}` is used unless a real OS environment
variable with that name is set, which is in turn overridden by a
`-D` system property or command-line argument if one is passed. In
practice this service only ever relies on the first two: the shipped
default, or an environment variable set by whoever is running it
(`docker-compose.yml`'s `environment:` block locally, a platform secret
or app-service configuration in deployment). There is no
`application-<profile>.yml` split by environment — the same jar and the
same `application.yml` run everywhere, only the environment variables
around them change.

| Variable | Default value | Use |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/techcup_notifications` | MongoDB connection string (compatible with Azure Cosmos DB for MongoDB vCore) |
| `SERVER_PORT` | `8083` | Service HTTP port (matches `NOTIFICACIONES_SERVICE_URL`'s default in `am-matches-service`) |
| `INTERNAL_API_KEY` | `local-dev-internal-key` | Shared API key used to authenticate service-to-service event webhooks |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ broker host |
| `RABBITMQ_PORT` | `5674` locally (`5672` inside docker-compose) | AMQP port |
| `RABBITMQ_USERNAME` | `guest` | Broker user |
| `RABBITMQ_PASSWORD` | `guest` | Broker password — **in deployment this never goes into `application.yml` or `docker-compose.yml`**, it is injected as a platform secret. See [Service Integration](integracion-servicios.md#rabbitmq-shared-cloudamqp-broker) |
| `RABBITMQ_VHOST` | `/` | Broker virtual host |
| `RABBITMQ_SSL_ENABLED` | `false` | `true` on CloudAMQP (TLS required on port 5671) |
| `RABBITMQ_SHARED_EXCHANGE` | `techcup.exchange` | Name of the topic exchange shared across all TechCup microservices |

These variables are resolved in `src/main/resources/application.yml`. None
of them are optional in the sense of "the service degrades gracefully
without it" — every one has a default, but the defaults are only meant for
local development; running with production traffic on
`INTERNAL_API_KEY=local-dev-internal-key` or `RABBITMQ_PASSWORD=guest`
would defeat the authentication mechanisms described in
[Architecture](arquitectura.md#inter-service-communication-api-events).
`docker-compose.yml` sets each one explicitly for the
`service-notifications` container (pointing `MONGODB_URI` and
`RABBITMQ_HOST` at the `mongo`/`rabbitmq` service names on the compose
network instead of `localhost`), so local runs never depend on values
leaking in from your shell.

### Connecting to the shared CloudAMQP RabbitMQ (staging/production)

To point the service at the shared broker instead of the local RabbitMQ
from `docker-compose.yml`, export:

```bash
export RABBITMQ_HOST=<CloudAMQP host — ask the team via a private channel>
export RABBITMQ_PORT=5671
export RABBITMQ_USERNAME=<CloudAMQP user — ask the team via a private channel>
export RABBITMQ_VHOST=<CloudAMQP vhost — ask the team via a private channel>
export RABBITMQ_SSL_ENABLED=true
export RABBITMQ_PASSWORD=<ask Juan David Rangel Jiménez privately>
```

None of these values (host, user, vhost, or password) **live in this
repository, nor should they ever**, (not in `application.yml`, not in
`docker-compose.yml`, not in a commit, not in a public chat message) —
since this is a public repository, publishing the shared broker's
connection details here would make brute-force attacks against the
password easier. They are requested through the team's private channel and
injected only as environment variables or deployment-platform secrets
(GitHub Actions secret, Azure App Service configuration, etc.). Local
(`docker compose up`) always keeps using the project's own RabbitMQ and
never needs this password.

## Testing protected endpoints

This service exposes two distinct security schemes in Swagger, both
available from the **Authorize** button:

- **`internalApiKey`**: paste the value of `INTERNAL_API_KEY` (defaults to
  `local-dev-internal-key`). Enables the 9 event webhooks.
- **`bearerAuth`**: since this service trusts that the API Gateway already
  validated the signature (`JwtClaimsFilter` only reads the `sub` claim, it
  does not re-verify it), you don't need a real, signed JWT to test
  locally. Generate one with a valid shape:

  ```bash
  ./scripts/generate-test-jwt.sh 33333333-3333-3333-3333-333333333333
  ```

  Paste the result into `bearerAuth`. See the full step-by-step guide in
  the repository's `README.md`.

## Documentation (MkDocs)

This service's technical documentation is built with
[MkDocs](https://www.mkdocs.org/) and the
[Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) theme,
just like in `am-matches-service` and `am-logistic-service`.

### Installation

```bash
python -m venv .venv
# Linux / macOS
source .venv/bin/activate
# Windows (PowerShell)
.venv\Scripts\Activate.ps1

pip install mkdocs-material
```

### Serving the documentation locally

```bash
mkdocs serve
```

Starts a local server at
[http://127.0.0.1:8000](http://127.0.0.1:8000) with live reload.

### Building the static site

```bash
mkdocs build
```

Generates the site under `site/` (a folder ignored by git).

### Documentation structure

```
project/
│
├── docs/
│   ├── index.md
│   ├── introduccion.md
│   ├── requerimientos.md
│   ├── configuracion.md
│   ├── arquitectura.md
│   ├── api.md
│   ├── integracion-servicios.md
│   ├── pruebas.md
│   ├── equipo.md
│   ├── anexos.md
│   └── assets/
│       ├── img/
│       ├── diagrams/
│       └── stylesheets/
│           └── extra.css
│
├── mkdocs.yml
├── src/
```

The theme's colors and typography (TechCup's purple/gold palette) are
defined in `docs/assets/stylesheets/extra.css` and declared in
`mkdocs.yml` under `extra_css` — the same file used by the other two
services on the team, so all three sites look consistent.
