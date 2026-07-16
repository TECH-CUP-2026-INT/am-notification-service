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

Levanta MongoDB (con healthcheck) y la app ya conectada a él. Los índices se
crean automáticamente al arrancar. Cuando ambos contenedores estén
arriba:

- Swagger UI: [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)
- Health: [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)
- MongoDB queda expuesto en el host en `27019` (no `27017`, para no chocar con
  el Mongo de `am-matches-service` ni con otro Mongo local).

`docker compose down` para apagar todo; agrega `-v` si además quieres borrar
los datos persistidos.

### Opción 2: Maven local

```bash
./mvnw spring-boot:run
```

Requiere una instancia de MongoDB accesible; usa las variables de entorno
de abajo para apuntarlo a tu base.

## Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/techcup_notifications` | Connection string de MongoDB (compatible con Azure Cosmos DB for MongoDB vCore) |
| `SERVER_PORT` | `8083` | Puerto HTTP del servicio (coincide con `NOTIFICACIONES_SERVICE_URL` por defecto en `am-matches-service`) |
| `INTERNAL_API_KEY` | *(obligatoria, sin default)* | API key compartida para autenticar los webhooks de eventos servicio-a-servicio. El servicio no arranca sin ella. En `docker-compose.yml` se fija en `local-dev-internal-key` para desarrollo local |

Estas variables se resuelven en `src/main/resources/application.yml`.

## Probar los endpoints protegidos

Este servicio expone dos esquemas de seguridad distintos en Swagger, ambos
disponibles desde el botón **Authorize**:

- **`internalApiKey`**: pega el valor configurado en `INTERNAL_API_KEY` (en local,
  `local-dev-internal-key` según `docker-compose.yml`). Habilita los 8 webhooks de eventos.
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
