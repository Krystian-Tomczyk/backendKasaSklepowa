package org.example.backendkasasklepowa.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// TEN PLIK TRZEBA PRZENIEŚĆ, NIE POWINIEN BYĆ W CONFIG

public class TerminalWebSocketHandler extends TextWebSocketHandler {

    // Lista podłączonych urządzeń (Kasa i Terminale)
    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Podłączono nowe urządzenie: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // BARDZO PROSTY ROUTER: Kiedy Kasa wyśle wiadomość, przekaż ją do Terminala i odwrotnie.
        System.out.println("Otrzymano WS: " + message.getPayload());

        for (WebSocketSession s : sessions) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
        System.out.println("Odłączono urządzenie: " + session.getId());
    }
}