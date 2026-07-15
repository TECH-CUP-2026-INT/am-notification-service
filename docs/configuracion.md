# Configuración

## Clonar el repositorio

```bash
git clone https://github.com/TECH-CUP-2026-INT/am-notification-service.git
cd am-notification-service
```

## Ejecutar el servicio localmente

### Opción 1: Docker Compose (recomendado)

```bash
docker compose up --build
```

Levanta MongoDB y RabbitMQ (ambos con healthcheck) y la app ya conectada a
ellos. Los índices y las colas se crean automáticamente al arrancar. Cuando
los tres contenedores estén arriba:

- Swagger UI: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- Health: [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)
- MongoDB queda expuesto en el host en `27019` (no `27017`, para no chocar con
  el Mongo de `am-matches-service` ni con otro Mongo local).
- RabbitMQ (local, para desarrollo/tests) queda expuesto en `5674` (AMQP) y
  `15674` (panel de administración, usuario/clave `guest`/`guest`). Este
  RabbitMQ local es independiente del broker compartido de CloudAMQP — ver
  [Arquitectura](arquitectura.md#rabbitmq-cloudamqp-compartido).

`docker compose down` para apagar todo; agrega `-v` si además quieres borrar
los datos persistidos.

### Opción 2: Maven local

```bash
./mvnw spring-boot:run
```

Requiere una instancia de MongoDB y una de RabbitMQ accesibles; usa las
variables de entorno de abajo para apuntarlo a tu infraestructura (`docker
compose up -d mongo rabbitmq` si solo quieres la infraestructura y correr la
app aparte con Maven).

## Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/techcup_notifications` | Connection string de MongoDB (compatible con Azure Cosmos DB for MongoDB vCore) |
| `SERVER_PORT` | `8083` | Puerto HTTP del servicio (coincide con `NOTIFICACIONES_SERVICE_URL` por defecto en `am-matches-service`) |
| `INTERNAL_API_KEY` | `local-dev-internal-key` | API key compartida para autenticar los webhooks de eventos servicio-a-servicio |
| `RABBITMQ_HOST` | `localhost` | Host del broker RabbitMQ |
| `RABBITMQ_PORT` | `5674` en local (`5672` dentro de docker-compose) | Puerto AMQP |
| `RABBITMQ_USERNAME` | `guest` | Usuario del broker |
| `RABBITMQ_PASSWORD` | `guest` | Contraseña del broker — **en despliegue nunca va en `application.yml` ni en `docker-compose.yml`**, se inyecta como secreto de la plataforma. Ver [Arquitectura](arquitectura.md#rabbitmq-cloudamqp-compartido) |
| `RABBITMQ_VHOST` | `/` | Virtual host del broker |
| `RABBITMQ_SSL_ENABLED` | `false` | `true` en CloudAMQP (TLS obligatorio en el puerto 5671) |
| `RABBITMQ_SHARED_EXCHANGE` | `techcup.exchange` | Nombre del exchange topic compartido entre todos los microservicios de TechCup |

Estas variables se resuelven en `src/main/resources/application.yml`.

### Conectarse al RabbitMQ compartido de CloudAMQP (staging/producción)

Para apuntar el servicio al broker compartido en vez del RabbitMQ local de
`docker-compose.yml`, exporta:

```bash
export RABBITMQ_HOST=<host de CloudAMQP — pídelo al equipo por canal privado>
export RABBITMQ_PORT=5671
export RABBITMQ_USERNAME=<usuario de CloudAMQP — pídelo al equipo por canal privado>
export RABBITMQ_VHOST=<vhost de CloudAMQP — pídelo al equipo por canal privado>
export RABBITMQ_SSL_ENABLED=true
export RABBITMQ_PASSWORD=<pídesela a Juan David Rangel Jiménez por privado>
```

Ninguno de estos valores (host, usuario, vhost ni contraseña) **está en este
repositorio ni debe llegar a estarlo** (ni en `application.yml`, ni en
`docker-compose.yml`, ni en un commit, ni en un mensaje de chat público) —
al ser un repositorio público, publicar aquí los datos de conexión del
broker compartido facilita ataques de fuerza bruta contra la contraseña. Se
piden por canal privado del equipo y se inyectan solo como variables de
entorno o secretos de la plataforma de despliegue (GitHub Actions
secret, Azure App Service configuration, etc.). Local (`docker compose up`)
sigue usando el RabbitMQ propio del proyecto y nunca necesita esta
contraseña.

## Probar los endpoints protegidos

Este servicio expone dos esquemas de seguridad distintos en Swagger, ambos
disponibles desde el botón **Authorize**:

- **`internalApiKey`**: pega el valor de `INTERNAL_API_KEY` (por defecto
  `local-dev-internal-key`). Habilita los 8 webhooks de eventos.
- **`bearerAuth`**: como este servicio confía en que el API Gateway ya
  validó la firma (`JwtClaimsFilter` solo lee el claim `sub`, no la
  reverifica), no necesitas un JWT real firmado para probar en local.
  Genera uno con forma válida:

  ```bash
  ./scripts/generate-test-jwt.sh 33333333-3333-3333-3333-333333333333
  ```

  Pega el resultado en `bearerAuth`. Ver la guía completa paso a paso en el
  `README.md` del repositorio.

## Documentación (MkDocs)

La documentación técnica de este servicio está construida con
[MkDocs](https://www.mkdocs.org/) y el tema
[Material for MkDocs](https://squidfunk.github.io/mkdocs-material/), igual
que en `am-matches-service` y `am-logistic-service`.

### Instalación

```bash
python -m venv .venv
# Linux / macOS
source .venv/bin/activate
# Windows (PowerShell)
.venv\Scripts\Activate.ps1

pip install mkdocs-material
```

### Servir la documentación en local

```bash
mkdocs serve
```

Levanta un servidor local en
[http://127.0.0.1:8000](http://127.0.0.1:8000) con recarga automática.

### Compilar el sitio estático

```bash
mkdocs build
```

Genera el sitio en `site/` (carpeta ignorada por git).

### Estructura de la documentación

```
proyecto/
│
├── docs/
│   ├── index.md
│   ├── introduccion.md
│   ├── requerimientos.md
│   ├── configuracion.md
│   ├── arquitectura.md
│   ├── api.md
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

Los colores y tipografía del tema (paleta morado/dorado de TechCup) están
definidos en `docs/assets/stylesheets/extra.css` y declarados en
`mkdocs.yml` bajo `extra_css` — el mismo archivo usado en los otros dos
servicios del equipo, para que los tres sitios se vean consistentes.
