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

Levanta Postgres (con healthcheck) y la app ya conectada a él. Flyway aplica
la migración automáticamente al arrancar. Cuando ambos contenedores estén
arriba:

- Swagger UI: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- Health: [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)
- Postgres queda expuesto en el host en `5433` (no `5432`, para no chocar con
  el Postgres de `am-matches-service` ni con otro Postgres local).

`docker compose down` para apagar todo; agrega `-v` si además quieres borrar
los datos persistidos.

### Opción 2: Maven local

```bash
./mvnw spring-boot:run
```

Requiere una instancia de PostgreSQL accesible (Flyway aplica la migración
al arrancar); usa las variables de entorno de abajo para apuntarlo a tu
base.

## Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `techcup_notifications` | Nombre de la base de datos |
| `DB_USER` | `postgres` | Usuario de la base de datos |
| `DB_PASSWORD` | `postgres` | Contraseña de la base de datos |
| `SERVER_PORT` | `8083` | Puerto HTTP del servicio (coincide con `NOTIFICACIONES_SERVICE_URL` por defecto en `am-matches-service`) |
| `INTERNAL_API_KEY` | `local-dev-internal-key` | API key compartida para autenticar los webhooks de eventos servicio-a-servicio |

Estas variables se resuelven en `src/main/resources/application.yml`.

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
