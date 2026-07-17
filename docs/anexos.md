# Appendices

## References

- [Architecture](arquitectura.md) — layers, design decisions, and component
  breakdown referenced throughout this appendix.
- [API](api.md) — REST endpoints and authentication schemes.
- [Configuration](configuracion.md) — environment variables and local setup.
- [Service Integration](integracion-servicios.md) — status of each producer
  integration.

## Bibliography

### Documentation tools

- [MkDocs](https://www.mkdocs.org/) — static documentation site generator
  used in this project.
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) —
  theme used for the site.
- [Spring Boot](https://spring.io/projects/spring-boot) — the service's
  application framework.
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb) —
  data access over MongoDB.
- [springdoc-openapi](https://springdoc.org/) — generates the OpenAPI
  specification and Swagger UI.
- [JaCoCo](https://www.jacoco.org/jacoco/) — test coverage.

## Glossary

| Term | Meaning |
|---|---|
| Event webhook | REST endpoint that receives, from another microservice, notice that something happened |
| `NotificationType` | Explicit enum of the notified event type; source of truth for accessibility, not a color/icon |
| Notification bell | UI metaphor for the user's history + unread count |
| `InternalServicePrincipal` | Security principal for service-to-service calls (never has a `userId`) |
| `AuthenticatedUser` | Security principal for the end user, built from the Gateway's JWT |
| Confirmed / proposed contract | A webhook is "confirmed" once the real producer (another microservice) already calls it and it has been verified end-to-end; "proposed" when the endpoint exists but the producer isn't connected yet |
