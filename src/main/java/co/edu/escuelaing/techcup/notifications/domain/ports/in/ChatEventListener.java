package co.edu.escuelaing.techcup.notifications.domain.ports.in;

import co.edu.escuelaing.techcup.notifications.domain.model.event.ChatMessageEvent;

/** Puerto de entrada para el evento de nuevo mensaje del Servicio de Comunicaciones. */
public interface ChatEventListener {

    void onNewChatMessage(ChatMessageEvent event);
}
