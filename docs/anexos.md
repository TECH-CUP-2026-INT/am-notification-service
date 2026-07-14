# Anexos

## Glosario

| Término | Significado |
|---|---|
| Webhook de evento | Endpoint REST que recibe, de otro microservicio, la notificación de que algo ocurrió |
| `NotificationType` | Enum explícito del tipo de evento notificado; fuente de verdad para accesibilidad, no un color/ícono |
| Campanita | Metáfora de UI para el historial + conteo de no leídas del usuario |
| `InternalServicePrincipal` | Principal de seguridad para llamadas servicio-a-servicio (nunca tiene `userId`) |
| `AuthenticatedUser` | Principal de seguridad para el usuario final, construido desde el JWT del Gateway |
| Contrato confirmado / propuesto | Un webhook está "confirmado" cuando el productor real (otro microservicio) ya lo llama y se verificó end-to-end; "propuesto" cuando el endpoint existe pero el productor aún no está conectado |

## Hallazgos de seguridad

- **Los webhooks de eventos aceptaban un JWT de usuario final en vez de
  exigir la API key interna (vulnerabilidad real)**: `SecurityConfig` solo
  exigía `.anyRequest().authenticated()`, y `JwtClaimsFilter` autentica
  cualquier JWT bien formado con claim `sub` como `ROLE_USER` — así que
  cualquier usuario autenticado podía hacer `POST
  /api/notificaciones/sanciones` (o cualquier otro webhook) con su propio
  JWT y crear notificaciones falsas suplantando a otro microservicio, sin
  necesitar la `X-Internal-Api-Key`. Corregido: `SecurityConfig` ahora exige
  explícitamente `ROLE_SERVICIO_INTERNO` en los 5 webhooks de eventos
  (`sanciones`, `mensajes`, `equipos/**`, `inscripciones/**`, `partidos`),
  que solo `InternalApiKeyFilter` concede. Verificado con test
  (`SanctionEventControllerTest.receive_withOnlyUserJwt_isRejected`).
- **`RestSanctionNotifier` de `am-matches-service` no enviaba el header
  `X-Internal-Api-Key`**: la única integración con un productor real propio
  del equipo (sanciones) fallaba en producción, porque
  `InternalApiKeyFilter` de este servicio rechaza cualquier request sin esa
  key. Corregido en `am-matches-service` durante esta auditoría — ver el
  [Historial de cambios](#historial-de-cambios) de ese repositorio.
- **Separación de mecanismos de autenticación**: `CurrentUserProvider`
  exige específicamente un principal `AuthenticatedUser`; un
  `InternalServicePrincipal` (autenticado solo con la API key) no puede
  leer el historial de notificaciones de nadie, y un JWT de usuario no
  autentica un webhook. Esto se verificó explícitamente con pruebas (ver
  [Pruebas](pruebas.md)) para que un cambio futuro no reintroduzca la
  confusión entre ambos.
- **Warning de credencial en memoria generada por Spring Boot**: ya estaba
  correctamente resuelto en este repositorio (exclusión de
  `UserDetailsServiceAutoConfiguration` en `application.yml`), mismo patrón
  que se replicó en `am-matches-service` y `am-logistic-service`.

**Implicación operativa (no negociable), igual que en los otros dos
servicios propios:** como este servicio confía en que el JWT ya fue
validado por el Gateway, **nunca debe exponerse directo a internet**. Debe
protegerse a nivel de red (firewall/security group/service mesh).

## Pipeline de CI/CD

Definido en
[`.github/workflows/ci-push.yml`](https://github.com/TECH-CUP-2026-INT/am-notification-service/blob/main/.github/workflows/ci-push.yml)
(más `pr-master.yml` y `pr-qa.yml`), calcado del pipeline ya probado de
`am-matches-service`. Etapas:

1. **Checkout** del código.
2. **Configuración del entorno**: JDK 21 (Temurin) con cache de Maven.
3. **Compilación** (`./mvnw compile`).
4. **Ejecución de pruebas** (`./mvnw test`) contra un contenedor de
   PostgreSQL levantado como servicio del workflow, con publicación del
   reporte de Surefire.
5. **Gate de cobertura** (`./mvnw jacoco:check`, mínimo 80% de línea), con
   publicación del reporte de JaCoCo.
6. **Análisis estático** con SonarQube, incluyendo el reporte de cobertura,
   omitido en pull requests desde forks.
7. **Empaquetado** del JAR y publicación como artefacto.
8. **Dockerización**: solo en eventos `push` a `master`/`qa`.
9. **Despliegue**: a Azure App Service, solo en `push` a `main`.

## Referencias

- [MkDocs](https://www.mkdocs.org/) — generador de sitios de documentación
  estática usado en este proyecto.
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) — tema
  usado para el sitio.
- [Spring Boot](https://spring.io/projects/spring-boot) — framework del
  servicio.
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb) — acceso a datos sobre MongoDB.
- [springdoc-openapi](https://springdoc.org/) — generación de la
  especificación OpenAPI y Swagger UI.
- [JaCoCo](https://www.jacoco.org/jacoco/) — cobertura de pruebas.

## Historial de cambios

| Fecha | Cambio |
|---|---|
| 2026-07-12 | Añadido gate de cobertura JaCoCo (80%), pipeline de CI/CD (calcado de matches-service) y documentación técnica en MkDocs. Cobertura de pruebas subida a 100% del bundle de lógica de negocio. Corregida vulnerabilidad real: los webhooks de eventos aceptaban un JWT de usuario en vez de exigir la API key interna — ver [Hallazgos de seguridad](#hallazgos-de-seguridad). Detectado y documentado el hueco funcional de "cesión de capitanía" (sin endpoint) — ver [Requerimientos](requerimientos.md). |
