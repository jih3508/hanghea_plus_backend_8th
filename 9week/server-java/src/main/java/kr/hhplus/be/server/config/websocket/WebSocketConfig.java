package kr.hhplus.be.server.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정 - /topic, /queue 접두사로 메시지 구독
        config.enableSimpleBroker("/topic", "/queue");
        // 클라이언트에서 메시지 발송시 /app 접두사 사용
        config.setApplicationDestinationPrefixes("/app");
        // 사용자별 개인 메시지 접두사
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트 설정
        registry.addEndpoint("/ws/coupon")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
