package com.forgemind.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket / STOMP configuration.
 *
 * <p>Phase 1: Uses the in-memory simple broker. In Phase 2+ (horizontal scaling),
 * this should be replaced with a {@code enableStompBrokerRelay} pointing at a
 * RabbitMQ or ActiveMQ instance as described in {@code PROJECT_RISKS.md} (C-001).
 *
 * <p>Topics:
 * <ul>
 *   <li>{@code /topic/generation.*} — AI generation progress events</li>
 *   <li>{@code /topic/chat.*}       — Workspace AI chat replies</li>
 *   <li>{@code /topic/build.*}      — Build log streaming events</li>
 * </ul>
 *
 * <p>Application destination prefix: {@code /app}
 * e.g. client sends to {@code /app/chat.send}, server routes to {@code @MessageMapping("/chat.send")}
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // In-memory broker — replace with StompBrokerRelay for horizontal scaling (Phase 2+)
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }
}
