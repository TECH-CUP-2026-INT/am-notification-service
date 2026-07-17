# Appendices

## References

Quick links to the external systems and frameworks referenced throughout
this documentation:

- [Spring Boot](https://spring.io/projects/spring-boot) — the service's
  application framework.
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb) —
  data access over MongoDB.
- [Spring AMQP](https://spring.io/projects/spring-amqp) — RabbitMQ
  integration (`spring-boot-starter-amqp`).
- [Spring Security](https://spring.io/projects/spring-security) — the JWT
  and internal-API-key filter chain.
- [springdoc-openapi](https://springdoc.org/) — generation of the OpenAPI
  specification and Swagger UI.
- [RabbitMQ](https://www.rabbitmq.com/) — the message broker used for the
  events entry point.
- [CloudAMQP](https://www.cloudamqp.com/) — the managed RabbitMQ provider
  for the broker shared across TechCup's own microservices.
- [MongoDB](https://www.mongodb.com/) — the service's database.
- [JaCoCo](https://www.jacoco.org/jacoco/) — the test-coverage gate
  enforced in CI.
- [Docker](https://www.docker.com/) / [Docker Compose](https://docs.docker.com/compose/) —
  containerization and local orchestration.
- [MkDocs](https://www.mkdocs.org/) — the generator behind this
  documentation site.
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) —
  the theme used for the site.

## Bibliography

- Broadcom / VMware Tanzu. (2026). *Spring Boot Reference Documentation*
  (v3.5). Retrieved from https://docs.spring.io/spring-boot/index.html
- Broadcom / VMware Tanzu. (2026). *Spring Data MongoDB Reference
  Documentation*. Retrieved from https://docs.spring.io/spring-data/mongodb/reference/
- Broadcom / VMware Tanzu. (2026). *Spring AMQP Reference Documentation*.
  Retrieved from https://docs.spring.io/spring-amqp/reference/
- Broadcom / VMware Tanzu. (2026). *Spring Security Reference
  Documentation*. Retrieved from https://docs.spring.io/spring-security/reference/
- MongoDB, Inc. (2026). *MongoDB Manual*. Retrieved from
  https://www.mongodb.com/docs/manual/
- VMware / Broadcom. (2026). *RabbitMQ Documentation*. Retrieved from
  https://www.rabbitmq.com/docs
- 84codes AB. (2026). *CloudAMQP Documentation*. Retrieved from
  https://www.cloudamqp.com/docs/index.html
- OpenAPI Initiative. (2026). *springdoc-openapi Documentation*. Retrieved
  from https://springdoc.org/
- Eclipse Foundation (JaCoCo project). (2026). *JaCoCo Java Code Coverage
  Library Documentation*. Retrieved from https://www.jacoco.org/jacoco/trunk/doc/
- Docker, Inc. (2026). *Docker Documentation*. Retrieved from
  https://docs.docker.com/

### Documentation tools

- Kollmar, T., et al. (2026). *MkDocs Documentation*. Retrieved from
  https://www.mkdocs.org/
- Lienert, M. (squidfunk). (2026). *Material for MkDocs Documentation*.
  Retrieved from https://squidfunk.github.io/mkdocs-material/

## Glossary

| Term | Meaning |
|---|---|
| Event webhook | REST endpoint that receives, from another microservice, the notification that something happened |
| `NotificationType` | Explicit enum for the type of notified event; the source of truth for accessibility, not a color/icon |
| Bell | UI metaphor for the user's history + unread count |
| `InternalServicePrincipal` | Security principal for service-to-service calls (never has a `userId`) |
| `AuthenticatedUser` | Security principal for the end user, built from the Gateway's JWT |
| Confirmed / proposed contract | A webhook is "confirmed" when the real producer (another microservice) already calls it and it has been verified end-to-end; "proposed" when the endpoint exists but the producer isn't connected yet |
| Composer | Domain service that translates one event type's record into a `CreateNotificationCommand` (see [Architecture](arquitectura.md#design-patterns)) |
| DLQ (dead-letter queue) | Queue that receives a RabbitMQ message after it has exhausted its retries, for manual inspection instead of silent loss |
