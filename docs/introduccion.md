# Introducción

## Contexto

**TechCup Fútbol** es un torneo universitario cuya plataforma digital, **Astro
Merge**, está compuesta por alrededor de 12 microservicios independientes,
cada uno responsable de un dominio de negocio acotado.

El **Servicio de Notificaciones** (`service-notifications`) es el
microservicio encargado de centralizar las alertas in-app del usuario final:
recibe eventos de otros microservicios (sanciones, mensajes de chat,
solicitudes/invitaciones de equipo, cambios de inscripción, programación de
partidos), los traduce a una notificación persistida, y expone el historial
al frontend.

## Propósito

Darle al usuario final una campanita de notificaciones confiable, de forma
que:

- Cada evento relevante de otro servicio produzca, a lo sumo, una
  notificación clara y accesible (nunca depende solo de color/ícono, ver
  [Anexos](anexos.md)).
- El usuario pueda consultar su historial, filtrarlo por leídas/no leídas, y
  marcar notificaciones como leídas (una o todas).
- Un fallo o retraso de este servicio nunca bloquee al servicio que originó
  el evento — los webhooks responden `202 Accepted` y la notificación se
  procesa de forma independiente.

## Dos actores, dos mecanismos de autenticación

Este servicio tiene dos tipos de "cliente" completamente distintos:

1. **Otros microservicios**, que envían eventos vía webhooks REST
   autenticados con una API key interna compartida (`X-Internal-Api-Key`).
2. **El usuario final** (a través del frontend), que consulta su historial
   de notificaciones autenticado con el JWT que ya validó el API Gateway.

Ambos mecanismos conviven en la misma cadena de filtros de Spring Security —
ver [Arquitectura](arquitectura.md).

## Alcance

### Qué SÍ hace este servicio

1. Recibir eventos de sanción por tarjetas desde el Servicio de Partidos
   (`am-matches-service`, integración propia del equipo astromerge,
   **confirmada**).
2. Recibir (una vez los equipos dueños confirmen el contrato) eventos de
   chat, solicitudes/invitaciones de equipo, cambios de inscripción y
   programación de partidos.
3. Traducir cada evento a una notificación persistida con un
   `NotificationType` explícito y un mensaje legible.
4. Exponer el historial de notificaciones del usuario autenticado, el
   conteo de no leídas, y el marcado de leídas (una o todas).

### Qué NO hace (responsabilidad de otros servicios)

| Responsabilidad | Servicio dueño |
|---|---|
| Decidir *cuándo* ocurre un evento notificable | El servicio de origen (Partidos, Comunicaciones, Equipos, Inscripción, Agendamiento) |
| Envío de push/email real fuera de la app | Fuera de alcance de la plataforma actual |
| Autenticación y validación de firma del JWT de usuario | API Gateway |

Ver [Arquitectura](arquitectura.md) para el detalle de qué integraciones
están confirmadas y cuáles siguen pendientes de otros equipos.
