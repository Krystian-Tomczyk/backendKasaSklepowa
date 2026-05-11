package org.example.backendkasasklepowa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// TEN PLIK TRZEBA PRZENIEŚĆ, NIE POWINIEN BYĆ W CONFIG

@Configuration
@EnableWebSocket
public class TerminalWebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Adres, pod który podłączy się Kasa i Terminal
        registry.addHandler(new TerminalWebSocketHandler(), "/ws/terminal").setAllowedOrigins("*");
    }
}