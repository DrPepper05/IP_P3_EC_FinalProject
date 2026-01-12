package com.luggagestorage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time updates.
 * Enables STOMP protocol over WebSocket for broadcasting booking events and locker availability changes.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for pub/sub messaging.
     *
     * @param registry Message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple broker for sending messages to clients
        // Clients subscribe to topics with "/topic" prefix
        registry.enableSimpleBroker("/topic");

        // Messages from clients with "/app" prefix are routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register STOMP endpoints for WebSocket connections.
     *
     * @param registry STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint at /ws
        // Enable SockJS fallback for browsers that don't support WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Allow all origins for development
                .withSockJS();  // Enable SockJS fallback

        // Also register without SockJS for native WebSocket support
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}
