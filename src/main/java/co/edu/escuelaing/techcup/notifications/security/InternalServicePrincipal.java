package co.edu.escuelaing.techcup.notifications.security;

/**
 * Principal para llamadas servicio-a-servicio autenticadas con la API key interna
 * (Sanciones, Comunicaciones, Equipos, Inscripción, Agendamiento). Deliberadamente no
 * tiene un userId: nunca debe poder pasar por CurrentUserProvider.
 */
public record InternalServicePrincipal() {
}
