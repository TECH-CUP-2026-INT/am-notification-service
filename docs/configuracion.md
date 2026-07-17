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

Starts MongoDB (with a health check) and the app already wired to it,
plus the observability stack (Prometheus, Grafana, Zipkin — see
[Architecture](arquitectura.md)). Indexes are created automatically on
startup. Once the containers are up:

- Swagger UI: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- Health: [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)
- MongoDB is exposed on the host at `27019` (not `27017`, to avoid
  colliding with `am-matches-service`'s Mongo or another local Mongo).
- Grafana at `http://localhost:3001` (`admin`/`admin` by default),
  Prometheus at `http://localhost:9091`, Zipkin at `http://localhost:9412`.

There is no local RabbitMQ container: this service connects straight to
the platform's shared CloudAMQP broker for the two event types it
consumes from RabbitMQ (see below), even in local development. Without a
valid `RABBITMQ_PASS`, that connection will fail to authenticate — the app
still starts and the REST API works normally, only the RabbitMQ
consumption is affected (see
[Architecture](arquitectura.md#why-rabbitmq)).

`docker compose down` shuts everything down; add `-v` if you also want to
delete persisted MongoDB data.

### Option 2: Local Maven

```bash
./mvnw spring-boot:run
```

Requires a reachable MongoDB instance and `INTERNAL_API_KEY` set (the
service refuses to start without it — see below); use the environment
variables below to point it at your infrastructure
(`docker compose up -d mongo` if you only want MongoDB and want to run the
app separately with Maven).

## Environment variables

An environment variable is a key/value pair supplied by the process's
runtime environment (the shell, the container, the CI runner, the cloud
platform) rather than baked into the compiled artifact. This service
follows that [twelve-factor](https://12factor.net/config) approach to
configuration on purpose: nothing about *where* it will run — a laptop, a
CI runner, Azure App Service — is hardcoded into the jar. Every value that
changes between those environments (connection strings, secrets, ports,
feature toggles) is read from an environment variable instead, which
means the exact same build artifact can be promoted from local
development to production without being recompiled or repackaged — only
the variables around it change.

`application.yml` supplies a **local-development default** for most of
them, using Spring's property-placeholder syntax
`${VARIABLE_NAME:default-value}`, so a developer can clone the repo and
run it with no configuration at all. A few values that must never fall
back to a real default — `INTERNAL_API_KEY`, the RabbitMQ password — are
declared with `${VARIABLE_NAME}` and no fallback, so a missing variable
fails the startup loudly instead of silently running with a known,
guessable value.

Spring resolves these through its standard externalized-configuration
order, so — from lowest to highest precedence — the value in
`application.yml`'s `${...:default}` is used unless a real OS environment
variable with that name is set, which is in turn overridden by a `-D`
system property or command-line argument if one is passed. In practice
this service only ever relies on the first two: the shipped default (when
there is one), or an environment variable set by whoever is running it
(`docker-compose.yml`'s `environment:` block locally, a platform secret or
GitHub Actions secret elsewhere). There is no `application-<profile>.yml`
split by environment — the same jar and the same `application.yml` run
everywhere, only the environment variables around them change.

Where each variable is actually set depends on who's running the
service: locally it's `docker-compose.yml`'s `environment:` block (or a
shell export, for Option 2 below); in CI it's a GitHub Actions
[repository secret](https://docs.github.com/actions/security-guides/encrypted-secrets)
injected into the workflow; in the deployed environment it's an Azure App
Service application setting. Secrets (`INTERNAL_API_KEY`, `RABBITMQ_PASS`,
`MAIL_PASSWORD`) are never committed to the repository in any of those
three places — only their variable *names* appear in
`application.yml` and in the workflow files.

### Core

| Variable | Default value | Use |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/techcup_notifications` | MongoDB connection string (compatible with Azure Cosmos DB for MongoDB vCore) |
| `SERVER_PORT` | `8083` | Service HTTP port |
| `INTERNAL_API_KEY` | *(none — required)* | Shared API key that authenticates the 9 service-to-service event webhooks. The service **fails to start** without it — there is deliberately no fallback value, so a forgotten variable can't silently ship with a guessable key. `docker-compose.yml` sets it to `local-dev-internal-key` for local development. |

### Email

| Variable | Default value | Use |
|---|---|---|
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server host |
| `MAIL_PORT` | `587` | SMTP server port |
| `MAIL_USERNAME` | *(empty)* | SMTP auth username |
| `MAIL_PASSWORD` | *(empty)* | SMTP auth password |
| `MAIL_FROM` | `no-reply@techcup.com` | `From` address on outgoing notification emails |
| `NOTIFICATION_TEST_EMAIL` | *(empty)* | If set, **every** notification email is sent to this single address instead of the real recipient — there is no service yet that exposes a user's real email, so this is a placeholder for testing the send path end-to-end. If left empty, email sending is skipped entirely (the in-app notification is still created). |

### Messaging (RabbitMQ)

Unlike the other variables, host, username, and virtual host for the
shared CloudAMQP broker are **not** read from environment variables — they
are set directly in `application.yml`, since this platform's shared
broker address isn't treated as sensitive on its own. Only the password is
externalized:

| Variable | Default value | Use |
|---|---|---|
| `RABBITMQ_PASS` | *(empty)* | Password for the shared CloudAMQP broker (`techcup.exchange`). Never committed; injected as a GitHub Actions secret in CI and as a platform secret in deployment. Without it, the RabbitMQ connection fails to authenticate but the rest of the service is unaffected — see [Service Integration](integracion-servicios.md#rabbitmq-shared-cloudamqp-broker). |

### Observability

| Variable | Default value | Use |
|---|---|---|
| `ZIPKIN_ENDPOINT` | `http://localhost:9411/api/v2/spans` | Where trace spans are reported; `docker-compose.yml` points this at the bundled Zipkin container (`http://zipkin:9411/api/v2/spans`) |
| `TRACING_SAMPLING` | `1.0` | Fraction of requests traced (`1.0` = trace everything — fine for a low-traffic dev/demo environment) |

`docker-compose.yml` also accepts `PROMETHEUS_PORT`, `GRAFANA_PORT`,
`GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD`, and `ZIPKIN_PORT` to
customize the observability stack's host ports and Grafana credentials;
these are read by Compose itself; the application doesn't read them.

These variables are resolved in `src/main/resources/application.yml`.
Running with production traffic on the shipped defaults for
`INTERNAL_API_KEY` or without a real `RABBITMQ_PASS` would defeat the
mechanisms described in [Architecture](arquitectura.md) and
[Service Integration](integracion-servicios.md).

## Testing protected endpoints

This service exposes two distinct security schemes in Swagger, both
available from the **Authorize** button, plus a CSRF requirement on the
two `PATCH` endpoints that Swagger doesn't handle automatically:

- **`internalApiKey`**: paste the value of `INTERNAL_API_KEY` (`local-dev-
  internal-key` if you're using `docker-compose.yml`'s default). Enables
  the 9 event webhooks.
- **`bearerAuth`**: since this service trusts that the API Gateway already
  validated the signature (`JwtClaimsFilter` only reads the `sub` claim, it
  does not re-verify it), you don't need a real, signed JWT to test
  locally. Generate one with a valid shape:

  ```bash
  ./scripts/generate-test-jwt.sh 33333333-3333-3333-3333-333333333333
  ```

  Paste the result into `bearerAuth`.
- **CSRF (`X-XSRF-TOKEN`)**: required on `PATCH /api/notificaciones/{id}/leer`
  and `PATCH /api/notificaciones/leer-todas`. Swagger UI's **Try it out**
  doesn't read cookies or add this header for you, so those two endpoints
  need a manual request (e.g. with `curl -b`/`-c` to capture the
  `XSRF-TOKEN` cookie from any prior response and resend it as the
  header). See the repository's `README.md` for the full step-by-step
  guide.

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
