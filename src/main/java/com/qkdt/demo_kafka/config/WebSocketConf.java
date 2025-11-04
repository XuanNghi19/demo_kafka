package com.qkdt.demo_kafka.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConf implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Dùng WebSocket gốc (không dùng SockJS nữa)
        registry.addEndpoint("/wsm")
                .setAllowedOriginPatterns("*"); // Cho phép tất cả origin
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tin nhắn gửi từ client đến controller sẽ phải bắt đầu bằng /web
        registry.setApplicationDestinationPrefixes("/web");

        // Tin nhắn từ server gửi về client sẽ có prefix là /socket
        registry.enableSimpleBroker("/socket");
    }
}
