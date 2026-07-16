# API

## Documentación interactiva (Swagger UI)

Con el servicio corriendo:
[http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

La especificación OpenAPI cruda está disponible en `/v3/api-docs`.

## Autenticación

Este servicio expone **dos** esquemas de seguridad distintos, ambos
disponibles desde el botón **Authorize** de Swagger:

- **`internalApiKey`** (header `X-Internal-Api-Key`): para los 10 webhooks de
  eventos servicio-a-servicio.
- **`bearerAuth`** (header `Authorization: Bearer <jwt>`): para los
  endpoints consultados por el usuario final. El JWT no necesita firma
  válida en desarrollo local (este servicio no la reverifica, esa es
  responsabilidad del Gateway) — genera uno con forma válida con
  `./scripts/generate-test-jwt.sh <uuid>`.

Ver [Configuración](configuracion.md) para la guía completa de cómo
autorizarte en Swagger.

## Endpoints consultados por el frontend (JWT del Gateway)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/notificaciones?leidas={true\|false}` | Historial del usuario autenticado (filtro opcional) |
| GET | `/api/notificaciones/no-leidas/conteo` | Conteo para el ícono de campanita |
| PATCH | `/api/notificaciones/{id}/leer` | Marca una notificación como leída |
| PATCH | `/api/notificaciones/leer-todas` | Marca todas como leídas |

## Webhooks de eventos (API key interna)

| Origen | Endpoint | Estado del contrato |
|---|---|---|
| Servicio de Partidos (`am-matches-service`) | `POST /api/notificaciones/sanciones` | ✅ Confirmado |
| Servicio de Torneos (`mk-tournament-service`) | `POST /api/notificaciones/sanciones-conducta` | ✅ Confirmado |
| Servicio de Comunicaciones | `POST /api/notificaciones/mensajes` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/solicitudes` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/respuestas` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/invitaciones` | ⚠️ Propuesto |
| Servicio de Equipos | `POST /api/notificaciones/equipos/capitania` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/estado` | ⚠️ Propuesto |
| Servicio de Inscripción | `POST /api/notificaciones/inscripciones/comprobante` | ⚠️ Propuesto |
| Servicio de Agendamiento | `POST /api/notificaciones/partidos` | ⚠️ Propuesto |

Ver [Arquitectura](arquitectura.md#estado-de-las-integraciones-entrantes)
para el detalle de qué falta de cada lado.

## Ejemplo: webhook de sanción (contrato confirmado)

`POST /api/notificaciones/sanciones`

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

Responde `202 Accepted` sin cuerpo — el procesamiento (crear la
notificación) ocurre de forma independiente del llamador.

## Ejemplo: webhook de sanción por conducta (contrato confirmado)

`POST /api/notificaciones/sanciones-conducta`

```json
{
  "playerId": "33333333-3333-3333-3333-333333333333",
  "matchesSuspended": 2,
  "reason": "Agresión verbal a un árbitro tras la finalización del partido.",
  "occurredAt": "2026-07-15T20:00:00Z"
}
```

A diferencia del webhook de tarjetas, este no está ligado a un partido en
vivo: lo dispara el Servicio de Torneos cuando el Organizador aplica una
sanción por conducta (`SanctionType.CONDUCT`), no el Servicio de Partidos.
Responde `202 Accepted` sin cuerpo.

## Ejemplo: webhook de cesión de capitanía (contrato propuesto)

`POST /api/notificaciones/equipos/capitania`

```json
{
  "teamId": "22222222-2222-2222-2222-222222222222",
  "teamName": "Los Tigres",
  "currentCaptainId": "33333333-3333-3333-3333-333333333333",
  "newCaptainId": "44444444-4444-4444-4444-444444444444",
  "initiatedBy": "DELEGATION",
  "occurredAt": "2026-07-12T20:00:00Z"
}
```

`initiatedBy` determina el destinatario: `DELEGATION` notifica a
`newCaptainId` (el Capitán actual delegó el rol); `APPLICATION` notifica a
`currentCaptainId` (un jugador aplicó para ser Capitán). Responde `202
Accepted` sin cuerpo.

## Ejemplo: consultar el historial

`GET /api/notificaciones?leidas=false`

Response `200 OK` (lista de `NotificationResponse`):

```json
[
  {
    "id": "1f2e3d4c-5b6a-4978-8a9b-0c1d2e3f4a5b",
    "type": "SANCION_TARJETAS",
    "message": "Fuiste sancionado por acumulación de tarjetas amarillas.",
    "referenceId": "11111111-1111-1111-1111-111111111111",
    "read": false,
    "createdAt": "2026-07-11T20:00:01Z",
    "readAt": null
  }
]
```

## Errores

Los errores de negocio (notificación no encontrada, acceso denegado a una
notificación de otro usuario) se manejan de forma centralizada en
`GlobalExceptionHandler` (`@RestControllerAdvice`) y se devuelven con el DTO
`ErrorResponse`:

| Código | Causa |
|---|---|
| `400` | Validación de payload de un webhook (Bean Validation) |
| `401` | Sin autenticación válida para el endpoint (JWT para usuario, API key para webhook) |
| `403` | Autenticado con el mecanismo equivocado (p. ej. API key en un endpoint de usuario) |
| `404` | `NotificationNotFoundException` |
