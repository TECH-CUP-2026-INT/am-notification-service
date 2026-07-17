# 1. Nombre del servicio

**Servicio de Notificaciones — TechCup Fútbol** (`am-notification-service`)

# 2. Descripción del servicio

Microservicio del sistema TechCup Fútbol (torneo universitario). Es puramente un
**consumidor de eventos** de otros microservicios y un **productor de alertas in-app**
para el usuario final: nunca genera notificaciones por iniciativa propia, siempre
reacciona a algo que ocurrió en otro servicio. Recibe eventos por webhooks REST y,
para dos tipos de evento, directamente del broker RabbitMQ compartido de la
plataforma (`techcup.exchange`).

Es uno de los tres servicios del dominio **D3 — Operaciones y Comunicación** del
equipo **astromerge**, junto con `am-matches-service` y `am-logistic-service`.

# 3. Funcionalidades del servicio

1. **Recepción de eventos** de otros microservicios (sanciones por tarjetas,
   sanciones de conducta, mensajes de chat, solicitudes/respuestas/invitaciones
   de equipo, cambios de estado de inscripción, agendamiento de partidos) a
   través de **9 webhooks REST**.
2. **Notificaciones de resultados y torneo** consumidas directamente del
   exchange RabbitMQ compartido, sin necesidad de un webhook adicional.
3. **Historial de notificaciones** del usuario autenticado, filtrable por
   leídas/no leídas, con marcado individual o masivo como leída.
4. **Correo best-effort** por cada notificación creada, enviado de forma
   asíncrona para que un servidor de correo lento o caído nunca retrase la
   respuesta al servicio de origen.
5. **Aislamiento de fallos**: el servicio que origina el evento nunca se ve
   bloqueado por este servicio (`202 Accepted` en los webhooks, consumo
   best-effort desde RabbitMQ).
6. **Autenticación diferenciada** entre llamadas servicio-a-servicio (API key
   interna) y usuario final (JWT del Gateway + CSRF), en la misma cadena de
   filtros de Spring Security.

# 4. Badges

[![CI](https://github.com/TECH-CUP-2026-INT/am-notification-service/actions/workflows/ci-push.yml/badge.svg)](https://github.com/TECH-CUP-2026-INT/am-notification-service/actions/workflows/ci-push.yml)
[![Docs](https://img.shields.io/badge/docs-mkdocs-6a1b9a)](https://tech-cup-2026-int.github.io/am-notification-service/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=TECH-CUP-2026-INT_am-notification-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=TECH-CUP-2026-INT_am-notification-service)

# 5. Integrantes del servicio

| Nombre | Contacto |
|---|---|
| Tomas Quiceno Ostos | tomas.quiceno-o@mail.escuelaing.edu.co |
| Sara Viviana Arteaga Rodríguez | sara.arteaga.r91@gmail.com |
| Julian Tinjaca | julian.tinjaca-c@mail.escuelaing.edu.co |
| Johan Beltrán | — |

# 6. Introducción rápida para probar

```bash
docker compose up --build
```

Levanta MongoDB (con healthcheck) y la app ya conectada a él. Los índices se crean
automáticamente al arrancar. Cuando ambos contenedores estén arriba:

- Swagger UI: http://localhost:8083/swagger-ui/index.html
- Health: http://localhost:8083/actuator/health
- MongoDB queda expuesto en `localhost:27019` (no `27017`, para no chocar con otro
  Mongo que ya tengas corriendo local) por si quieres conectarte con un cliente.

`docker compose down` para apagar todo; agrega `-v` si además quieres borrar los
datos persistidos. Ver la [guía completa de pruebas en Swagger](#guía-completa-de-pruebas-en-swagger)
más abajo para disparar los 8 eventos de ejemplo y validar la seguridad, y
[Pruebas](#pruebas) para la suite automatizada (reflejada en el badge de CI).

# 7. Tecnologías usadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 3.5.6 | Framework de aplicación |
| Spring Web | — | API REST (usuario final y webhooks) |
| Spring Data MongoDB | — | Persistencia orientada a documentos |
| MongoDB | — | Base de datos (compatible con Azure Cosmos DB for MongoDB vCore) |
| Spring Security | — | JWT del Gateway (usuario), API key interna (webhooks) y CSRF basado en cookie |
| Spring AMQP | — | Consume eventos de partido/torneo del broker CloudAMQP compartido |
| Spring Mail | — | Correo best-effort por cada notificación creada |
| Micrometer + Prometheus + Zipkin | — | Métricas y trazabilidad distribuida (observabilidad) |
| springdoc-openapi | 2.7.0 | Swagger UI / especificación OpenAPI |
| Lombok | — | Reducción de código boilerplate |
| Maven (wrapper `mvnw`) | — | Gestión de dependencias y build |
| Testcontainers | — | MongoDB real en pruebas de integración |
| JaCoCo | 0.8.12 | Cobertura de pruebas (≥ 80%) en CI |
| Docker + Docker Compose | — | Contenerización y orquestación local (app + MongoDB + observabilidad) |
| MkDocs (Material) | — | Documentación técnica extendida ([`docs/`](docs/)) |

---

## Arquitectura

Capas: `controller` → `listener` (puerto de entrada) → `service` → `repository`.

- **`controller/events/*`**: webhooks REST que exponen un endpoint por evento de
  origen. Solo deserializan y validan el payload HTTP.
- **`listener/*`**: interpretan el evento de dominio (sanción, mensaje, invitación...)
  y lo traducen a un `CreateNotificationCommand`. No conocen el repositorio ni el
  modelo de persistencia.
- **`messaging/*`**: consumidores `@RabbitListener` para los dos tipos de evento que
  llegan directo del broker compartido en vez de por webhook.
- **`service/NotificationServiceImpl`**: no sabe de dónde vino la notificación; solo
  persiste y resuelve las consultas del historial (campanita).
- **`security/*`**: dos mecanismos de autenticación conviven en la misma cadena de
  filtros: `JwtClaimsFilter` decodifica el JWT que ya validó el API Gateway (para los
  endpoints de usuario) e `InternalApiKeyFilter` valida una API key compartida (para
  los webhooks de eventos servicio-a-servicio).

Esta separación permite reemplazar el transporte (REST → cola de eventos, por
ejemplo) sin tocar la lógica de negocio: solo cambiaría el `controller`.

Diagramas de contexto, componentes y clases (editables en
[draw.io](https://app.diagrams.net/)) en
[`docs/assets/diagrams/`](docs/assets/diagrams/); versión Mermaid renderizada
en [`docs/arquitectura.md`](docs/arquitectura.md).

## Decisiones de integración

- **Mecanismo**: REST síncrono para los webhooks, más consumo directo de RabbitMQ
  para los dos eventos que ya se publican en el exchange compartido (ver
  `docs/arquitectura.md`).
- **Seguridad de los webhooks**: header `X-Internal-Api-Key` (configurable vía
  `INTERNAL_API_KEY`), porque esos endpoints reciben POSTs servicio-a-servicio sin JWT
  de usuario. Están exentos de CSRF (ver abajo): no los origina un navegador.
- **CSRF en los endpoints de usuario**: `PATCH /api/notificaciones/{id}/leer` y
  `PATCH /api/notificaciones/leer-todas` exigen el header `X-XSRF-TOKEN`. El servicio
  es `STATELESS` (sin sesión de servidor), así que el token viaja en una cookie
  (`XSRF-TOKEN`, `HttpOnly=false`) que el frontend debe leer con JS y reenviar como
  header en cada `PATCH`/`POST`/`PUT`/`DELETE` — es el patrón que Spring Security
  recomienda para SPAs sin sesión.
- **Persistencia**: MongoDB (Spring Data MongoDB), sin migraciones versionadas — los
  índices se crean automáticamente al arrancar (`auto-index-creation: true`).

## Modelo de datos

Colección `notification`: `id`, `recipientId` (destinatario), `type` (tipo de evento,
enum explícito — pensado para accesibilidad, no solo color/ícono), `message`, `referenceId`
(id del recurso relacionado, para que el frontend navegue), `read`, `createdAt`, `readAt`.

`NotificationType` cubre los requerimientos funcionales con 16 valores (se separan
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
| Servicio de Agendamiento | `POST /api/notificaciones/partidos` | ⚠️ Propuesto |

Los payloads propuestos están documentados en el Javadoc de cada clase en
`dto/event/*`, junto con las preguntas abiertas para el equipo dueño. Ver
[`docs/integracion-servicios.md`](docs/integracion-servicios.md) para el detalle
completo de qué integraciones están confirmadas.

## Configuración

Variables de entorno:

| Variable | Default |
|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/techcup_notifications` |
| `SERVER_PORT` | `8083` (coincide con el `NOTIFICACIONES_SERVICE_URL` por defecto que ya usa `service-match`) |
| `INTERNAL_API_KEY` | *(obligatoria, sin default — el servicio no arranca sin ella; `docker-compose.yml` la fija en `local-dev-internal-key` para desarrollo local)* |

## Cómo correrlo

### Con Docker (recomendado para probarlo o mostrárselo al equipo)

```bash
docker compose up --build
```

Levanta MongoDB (con healthcheck) y la app ya conectada a él. Los índices se crean
automáticamente al arrancar. Cuando ambos contenedores estén arriba:

- Swagger UI: http://localhost:8083/swagger-ui/index.html
- Health: http://localhost:8083/actuator/health
- MongoDB queda expuesto en `localhost:27019` (no `27017`, para no chocar con otro
  Mongo que ya tengas corriendo local) por si quieres conectarte con un cliente.

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

- **`internalApiKey`**: pega `local-dev-internal-key` (es el valor de
  `INTERNAL_API_KEY` fijado en `docker-compose.yml`). Con esto quedan habilitados los 8
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
- **`PATCH /api/notificaciones/{id}/leer`** y **`PATCH /api/notificaciones/leer-todas`**
  requieren además el header CSRF `X-XSRF-TOKEN` (ver sección de CSRF más arriba) —
  Swagger UI no lo agrega solo, así que estos dos no se pueden probar con el botón
  **Try it out** sin ese paso manual.

#### 4. Confirmar que la seguridad está bloqueando lo que debe

- Quita la API key del Authorize y repite un `POST` de evento → `403`.
- Deja solo el JWT (sin API key) y prueba un `POST` de evento → también `403` (el JWT
  no sirve para autenticar webhooks).
- Deja solo la API key (sin JWT) y prueba un `GET /api/notificaciones` → `401`, porque
  `CurrentUserProvider` exige específicamente un principal de tipo usuario, no de
  servicio interno.

### Sin Docker

```bash
./mvnw spring-boot:run
```

Requiere una instancia de MongoDB accesible por tu cuenta; usa las variables de
entorno de la sección [Configuración](#configuración) para apuntarlo a tu base.

## Pruebas

```bash
./mvnw test
```

Cubre la lógica de negocio clave: construcción de la notificación a partir de cada
tipo de evento (un test por listener) y las reglas de `NotificationService`
(pertenencia del destinatario, idempotencia de "marcar como leída", conteo de no
leídas). El test de contexto completo (`ServiceNotificationsApplicationTests`)
levanta un contenedor MongoDB real vía Testcontainers, igual que en `service-match`.
El badge de CI al inicio de este README refleja el resultado de esta suite (y del
gate de cobertura JaCoCo ≥ 80%) en cada push.

## CI/CD

El pipeline en [`.github/workflows/ci-push.yml`](.github/workflows/ci-push.yml) se
dispara en cada `push` a `main`/`develop`/`feature/**`, y automatiza: build, pruebas
(con publicación del reporte), gate de cobertura JaCoCo, análisis estático con
SonarQube, empaquetado del JAR, dockerización, publicación en GHCR y despliegue a
Azure App Service (cuando corresponde). Un segundo workflow,
[`.github/workflows/deploy-mkdocs.yml`](.github/workflows/deploy-mkdocs.yml), publica
la documentación en GitHub Pages en cada push a `main`.

## Documentación completa

La documentación técnica extendida (introducción, requerimientos, configuración,
arquitectura y diagramas, API, integración de servicios, pruebas, equipo, anexos)
está construida con [MkDocs](https://www.mkdocs.org/) y vive en [`docs/`](docs/),
publicada en <https://tech-cup-2026-int.github.io/am-notification-service/>.

Para servirla en local:

```bash
pip install -r requirements.txt
mkdocs serve
```

Ver [`docs/arquitectura.md`](docs/arquitectura.md) para la arquitectura en detalle,
y [`docs/assets/diagrams/`](docs/assets/diagrams/) para los diagramas fuente en
formato draw.io (`.drawio`, XML editable en
[app.diagrams.net](https://app.diagrams.net/)).
