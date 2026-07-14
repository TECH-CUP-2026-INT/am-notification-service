# Arquitectura

## Capas

`controller` → `listener` (puerto de entrada) → `service` → `repository`.

- **`controller/events/*`**: webhooks REST que exponen un endpoint por
  evento de origen. Solo deserializan y validan el payload HTTP.
- **`listener/*`**: interpretan el evento de dominio (sanción, mensaje,
  invitación...) y lo traducen a un `CreateNotificationCommand`. No conocen
  el repositorio ni el modelo de persistencia.
- **`service/NotificationServiceImpl`**: no sabe de dónde vino la
  notificación; solo persiste y resuelve las consultas del historial
  (campanita).
- **`security/*`**: dos mecanismos de autenticación conviven en la misma
  cadena de filtros — ver más abajo.

Esta separación permite reemplazar el transporte (REST → cola de eventos,
por ejemplo) sin tocar la lógica de negocio: solo cambiaría el `controller`.

## Modelo de datos

Tabla `notification`: `id`, `recipient_id` (destinatario), `type` (tipo de
evento, enum explícito — pensado para accesibilidad, no solo color/ícono),
`message`, `reference_id` (id del recurso relacionado, para que el frontend
navegue), `is_read`, `created_at`, `read_at`.

`NotificationType` cubre los requerimientos funcionales con valores
explícitos (se separan aprobada/rechazada/cancelada y
programado/reprogramado/cancelado en constantes distintas, no un tipo
genérico + campo de estado, para que el tipo por sí solo sea semánticamente
inequívoco para un lector de pantalla).

## Seguridad

Dos mecanismos de autenticación conviven en la misma cadena de filtros de
Spring Security (`config/SecurityConfig.java`), en el mismo modelo de
confianza que `am-matches-service` y `am-logistic-service` (el API Gateway
ya validó la firma del JWT; este servicio no la revalida):

- **`security/JwtClaimsFilter.java`**: decodifica el claim `sub` del JWT
  para los endpoints consultados por el usuario final
  (`/api/notificaciones/**` de lectura/marcado).
- **`security/InternalApiKeyFilter.java`**: valida el header
  `X-Internal-Api-Key` para los 9 webhooks de eventos servicio-a-servicio,
  autenticando como `InternalServicePrincipal` con `ROLE_SERVICIO_INTERNO`.
- **`security/CurrentUserProvider.java`**: exige específicamente un
  principal de tipo `AuthenticatedUser` — un `InternalServicePrincipal`
  (autenticado solo con la API key) **no** puede leer el historial de
  notificaciones, y viceversa un JWT de usuario no autentica un webhook.

**Implicación operativa (no negociable), igual que en los otros dos
servicios propios:** este servicio nunca debe exponerse directo a internet
ni a otros servicios que no sea el API Gateway (para los endpoints de
usuario) y los servicios de origen autorizados (para los webhooks). Debe
protegerse a nivel de red.

## Por qué REST y no un broker de mensajería

Mismo razonamiento que en `am-matches-service` y `am-logistic-service`: se
evaluó una cola de eventos, pero la plataforma no tiene un broker
desplegado hoy, e introducir uno solo para este servicio habría exigido
cambios coordinados en los ~5 equipos dueños de los eventos de origen —
fuera del alcance de un solo repo. Se optó por REST síncrono con `202
Accepted` rápido, dejando la traducción evento→notificación aislada en la
capa `listener` para poder migrar el transporte más adelante sin tocar
`service`.

## Estado de las integraciones entrantes

Este servicio es **puramente un consumidor de eventos** — nunca dispara una
notificación por iniciativa propia. La siguiente tabla es la fuente de
verdad de qué integración está realmente conectada con un productor real
propio del equipo (astromerge) y cuál sigue esperando a que otro equipo
confirme su contrato:

| Origen | Endpoint | Estado |
|---|---|---|
| **Servicio de Partidos** (`am-matches-service`, propio de astromerge) | `POST /api/notificaciones/sanciones` | ✅ **Confirmado y verificado end-to-end** — `RestSanctionNotifier` en matches-service envía el header `X-Internal-Api-Key` en cada llamada (corregido en esta auditoría; antes no lo enviaba y la llamada fallaba con `401`) |
| Servicio de Comunicaciones | `POST /api/notificaciones/mensajes` | ⚠️ Propuesto — contrato definido de este lado, pendiente de que el equipo dueño lo confirme e implemente el productor |
| Servicio de Equipos | `POST /api/notificaciones/equipos/solicitudes` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/respuestas` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/invitaciones` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/capitania` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/estado` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/comprobante` | ⚠️ Propuesto |
| Servicio de Agendamiento / Torneos | `POST /api/notificaciones/partidos` | ⚠️ Propuesto |

Los payloads propuestos están documentados en el Javadoc de cada clase en
`dto/event/*`, junto con las preguntas abiertas para el equipo dueño (por
ejemplo: si `recipientId` siempre es un único usuario o si el origen debe
hacer fan-out por cada destinatario).

**Nota de alcance:** ninguno de los ítems "⚠️ Propuesto" es un hueco de
*este* repositorio — el endpoint, la validación y la traducción a
notificación ya están implementados y probados de este lado. Lo que falta
es un servicio externo (fuera de las tres repos del equipo astromerge) que
llame a ese endpoint. No hay nada más que este equipo pueda hacer para
"cerrar" esas integraciones sin acceso al código de esos otros servicios.

## Servicios propios de astromerge (D3) y sus puertos

| Servicio | Puerto app (Docker) | Puerto MongoDB (host) |
|---|---|---|
| `am-matches-service` | `8080` | `27017` |
| `am-notification-service` | `8083` | `27019` |
| `am-logistic-service` | `8085` | `27018` |

## Verificación de conectividad end-to-end

Se levantaron los 3 servicios a la vez (`docker compose up --build` en cada
repo, sin colisión de puertos) y se disparó una sanción real desde
`am-matches-service` (2 tarjetas amarillas al mismo jugador). La llamada
llegó a `POST /api/notificaciones/sanciones` autenticada con
`X-Internal-Api-Key`, y la notificación quedó visible al consultar `GET
/api/notificaciones` autenticado como el jugador sancionado. Esto confirma
en caliente (no solo con tests) que: (a) el fix del header en
`RestSanctionNotifier` funciona, y (b) el fix de seguridad de esta misma
auditoría —exigir `ROLE_SERVICIO_INTERNO` en los webhooks— no rompe la
integración legítima.
