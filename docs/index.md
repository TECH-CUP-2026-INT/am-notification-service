# Servicio de Notificaciones (service-notifications)

Microservicio Spring Boot puramente **consumidor de eventos** de otros
microservicios de **TechCup Fútbol** y **productor de alertas in-app**
(estilo campanita) para el usuario final: nunca genera notificaciones por
iniciativa propia, siempre reacciona a algo que ocurrió en otro servicio.

[Ver en GitHub](https://github.com/TECH-CUP-2026-INT/am-notification-service){ .md-button .md-button--primary }
[Explorar la API](api.md){ .md-button }

## Mapa de la documentación

| Sección | Contenido |
|---|---|
| [Introducción](introduccion.md) | Contexto, propósito y alcance del servicio |
| [Requerimientos](requerimientos.md) | Requisitos funcionales, no funcionales y prerrequisitos técnicos |
| [Configuración](configuracion.md) | Variables de entorno, ejecución local y despliegue con Docker |
| [Arquitectura](arquitectura.md) | Capas, modelo de datos y estado de cada integración entrante |
| [API](api.md) | Endpoints REST, autenticación y Swagger UI |
| [Pruebas](pruebas.md) | Estrategia de pruebas y cómo ejecutarlas |
| [Equipo](equipo.md) | Integrantes y roles del equipo TECH-CUP 2026 INT |
| [Anexos](anexos.md) | Glosario, hallazgos de seguridad y referencias |

## Resumen rápido

| Capa | Tecnología |
|---|---|
| Lenguaje / runtime | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Build | Maven |
| Persistencia | PostgreSQL + Spring Data JPA |
| Migraciones | Flyway |
| API | Spring Web (REST) + springdoc-openapi |
| Seguridad | Spring Security (JWT del Gateway para usuarios + API key interna para webhooks) |
| CI/CD | GitHub Actions (build, test, cobertura, análisis estático, empaquetado, Docker) |
| Documentación | MkDocs + Material for MkDocs |

## Inicio rápido

```bash
# Levanta Postgres y el servicio (Flyway crea el esquema automáticamente)
docker compose up --build
```

Con el servicio corriendo, explora la API en
[http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html).

Para más detalle, ver [Configuración](configuracion.md) y [API](api.md).
