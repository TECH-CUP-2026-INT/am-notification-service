# Introduction

## Context

**TechCup Fútbol** is a university tournament whose digital platform, **Astro
Merge**, is made up of about 12 independent microservices, each responsible
for a bounded business domain.

The **Notifications Service** (`service-notifications`) is the microservice
in charge of centralizing the end user's in-app alerts: it receives events
from other microservices (card and conduct sanctions, chat messages, team
requests/invitations/captaincy, enrollment status changes, match
scheduling), translates them into a persisted notification, sends a
best-effort email, and exposes the history to the frontend.

## Purpose

Give the end user a reliable notification bell, such that:

- Every relevant event from another service produces, at most, one clear
  and accessible notification (never relying only on color/icon — see
  [Appendices](anexos.md)).
- The user can browse their history, filter it by read/unread, and mark
  notifications as read (one or all).
- A failure or delay in this service never blocks the service that
  originated the event — webhooks respond `202 Accepted` immediately, and
  a failing email never affects the in-app notification, which is already
  persisted before the email is even attempted.

## Two actors, two authentication mechanisms

This service has two completely different types of "client":

1. **Other microservices**, which send events via REST webhooks
   authenticated with a shared internal API key (`X-Internal-Api-Key`).
2. **The end user** (through the frontend), who queries and updates their
   notification history authenticated with the JWT already validated by
   the API Gateway, plus a CSRF token for state-changing requests.

Both mechanisms coexist in the same Spring Security filter chain — see
[Architecture](arquitectura.md).

## Scope

### What this service DOES do

1. Receive card-sanction events from the Matches Service
   (`am-matches-service`) and conduct-sanction events from the Tournaments
   Service (`mk-tournament-service`) — both **confirmed** integrations
   owned by real producers.
2. Receive (once the owning teams confirm the contract) chat events, team
   requests/invitations/captaincy transfers, enrollment status changes,
   and match scheduling events.
3. Consume match-result and tournament-finalized events directly from the
   platform's shared RabbitMQ exchange.
4. Translate each event into a persisted notification with an explicit
   `NotificationType` and a readable message, and attempt to email it to
   the recipient.
5. Expose the authenticated user's notification history, unread count, and
   mark-as-read (single or bulk).

### What it does NOT do (owned by other services)

| Responsibility | Owning service |
|---|---|
| Deciding *when* a notifiable event occurs | The originating service (Matches, Tournaments, Communications, Teams, Enrollment, Scheduling) |
| Resolving a recipient's real email address | Currently a placeholder (see [Architecture](arquitectura.md)) — no service in the org exposes this yet |
| Authenticating and validating the signature of the user's JWT | API Gateway |

See [Service Integration](integracion-servicios.md) for the detail of
which integrations are confirmed and which are still pending from other
teams.
