# Requerimientos

Requisitos funcionales tomados de la hoja de requerimientos del equipo
**astromerge** (dominio D3 — Operaciones y Comunicación) para el Servicio de
Notificaciones, contrastados contra la implementación actual.

## Requisitos funcionales (hoja de requerimientos)

| ID | Requisito | Estado |
|---|---|---|
| RF-01 | Notificación de sanción por acumulación de tarjetas, solo al jugador sancionado | ✅ Implementado y **confirmado** con el productor real (`am-matches-service`) — `POST /api/notificaciones/sanciones` |
| RF-02 | Notificación de nuevo mensaje en chat, a todos los miembros activos | ⚠️ Endpoint implementado (`POST /api/notificaciones/mensajes`), contrato **propuesto** — pendiente de confirmar con el Servicio de Comunicaciones (otro equipo) |
| RF-03 | Notificación de solicitud de vinculación a equipo, al Capitán | ⚠️ Endpoint implementado (`POST /api/notificaciones/equipos/solicitudes`), contrato **propuesto** — pendiente del Servicio de Equipos |
| RF-04 | Notificación de respuesta a solicitud de vinculación, al jugador solicitante | ⚠️ Endpoint implementado (`POST /api/notificaciones/equipos/respuestas`), contrato **propuesto** — pendiente del Servicio de Equipos |
| RF-05 | Notificación de invitación a equipo, al jugador invitado | ⚠️ Endpoint implementado (`POST /api/notificaciones/equipos/invitaciones`), contrato **propuesto** — pendiente del Servicio de Equipos |
| RF-06 | Notificación de cambio de estado de inscripción, al Capitán | ⚠️ Endpoint implementado (`POST /api/notificaciones/inscripciones/estado`), contrato **propuesto** — pendiente del Servicio de Inscripción |
| RF-07 | Notificación de inscripción completada con comprobante, al Organizador | ⚠️ Endpoint implementado (`POST /api/notificaciones/inscripciones/comprobante`), contrato **propuesto** — pendiente del Servicio de Inscripción |
| RF-08 | Notificación de programación, reprogramación o cancelación de partido, al Capitán y jugadores afectados | ⚠️ Endpoint implementado (`POST /api/notificaciones/partidos`), contrato **propuesto** — pendiente del Servicio de Agendamiento/Torneos |
| RF-09 | Notificación de cesión del rol de capitanía del equipo | ✅ Endpoint implementado (`POST /api/notificaciones/equipos/capitania`), contrato **propuesto** — pendiente del Servicio de Equipos |
| RF-10 | Consulta de historial de notificaciones: cada usuario ve solo las suyas, filtrable | ✅ Implementado (`GET /api/notificaciones`, `?leidas=`) |

!!! note "Huecos funcionales cerrados"
    RF-09 (cesión de capitanía) quedó identificado como hueco durante una
    auditoría previa y ya fue implementado: `CaptaincyTransferEvent` cubre
    las dos direcciones del requerimiento (delegación por el Capitán actual
    → notifica al jugador elegido; aplicación de un jugador → notifica al
    Capitán actual) con los tipos `CAPITANIA_CEDIDA` y
    `CAPITANIA_SOLICITADA`. El resto de los "⚠️ propuestos" no son huecos de
    este repo: son endpoints ya construidos y probados de este lado, a la
    espera de que el equipo dueño del evento (otro dominio, fuera de
    astromerge) confirme el contrato exacto y empiece a llamarlos — ver la
    tabla de estado en [Arquitectura](arquitectura.md).

## Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | **Aislamiento de fallos**: este servicio nunca debe bloquear al servicio de origen; los webhooks responden `202 Accepted` de forma rápida. |
| RNF-02 | **Seguridad de red**: el servicio no verifica la firma del JWT (responsabilidad del Gateway), por lo que debe permanecer inaccesible fuera de la red interna de la plataforma. |
| RNF-03 | **Autenticación diferenciada**: los webhooks de eventos (servicio-a-servicio) y los endpoints de usuario final usan mecanismos de autenticación distintos y no intercambiables — un JWT de usuario no debe poder autenticar un webhook, ni viceversa. |
| RNF-04 | **Accesibilidad**: cada tipo de evento tiene un `NotificationType` explícito y semánticamente inequívoco (no un tipo genérico + color/ícono). |
| RNF-05 | **Privacidad**: un usuario solo puede consultar y marcar como leídas sus propias notificaciones. |
| RNF-06 | **Mantenibilidad**: el transporte (hoy REST) está desacoplado de la lógica de negocio detrás de la capa `listener`, para poder reemplazarlo (p. ej. por una cola de eventos) sin tocar `service`. |
| RNF-07 | **Reproducibilidad del build**: el proyecto debe compilar, probar y empaquetarse de forma determinista vía Maven Wrapper, tanto en local como en CI. |
| RNF-08 | **Cobertura de pruebas**: al menos 80% de cobertura de línea sobre la lógica de negocio (excluyendo DTOs, entidades de MongoDB y clases de configuración), verificado automáticamente en CI (JaCoCo). |

## Prerrequisitos técnicos

Para desarrollar y ejecutar el servicio localmente:

| Herramienta | Versión mínima | Uso |
|---|---|---|
| [Java (JDK)](https://adoptium.net/) | 21 | Compilación y ejecución del servicio |
| [Docker](https://www.docker.com/) / Docker Compose | 24+ | Base de datos MongoDB y contenedor de la aplicación (también usado por Testcontainers en las pruebas) |
| [Git](https://git-scm.com/) | 2.x | Control de versiones |
| Maven Wrapper (`mvnw`, incluido en el repo) | — | No requiere instalación de Maven local |

Para trabajar en la documentación:

| Herramienta | Versión mínima | Uso |
|---|---|---|
| [Python](https://www.python.org/) | 3.9+ | Requerido por MkDocs |
| [MkDocs](https://www.mkdocs.org/) + [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) | — | Generación del sitio de documentación |

Ver [Configuración](configuracion.md) para los pasos de instalación de cada
herramienta y las variables de entorno del servicio.
