package co.edu.escuelaing.techcup.notifications.listener;

import co.edu.escuelaing.techcup.notifications.dto.event.ChatMessageEvent;

/** Puerto de entrada para el evento de nuevo mensaje del Servicio de Comunicaciones. */
public interface ChatEventListener {

    void onNewChatMessage(ChatMessageEvent event);
}
