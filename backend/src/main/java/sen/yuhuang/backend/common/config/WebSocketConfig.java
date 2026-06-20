package sen.yuhuang.backend.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import sen.yuhuang.backend.common.filter.WebSocketHandshakeInterceptor;

/**
 * WebSocket 配置
 * 
 * 使用 STOMP 协议实现实时消息推送
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    private static final String[] ALLOWED_ORIGIN_PATTERNS = {
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://106.53.50.72:5173",
            "http://aa.junyuguoguo.xyz",
            "https://aa.junyuguoguo.xyz"
    };

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的消息代理，用于向客户端发送消息
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用程序目标前缀，客户端发送到这个前缀的消息会被路由到 @MessageMapping 方法
        registry.setApplicationDestinationPrefixes("/app");
        
        // 设置用户特定消息的前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 原生 WebSocket 端点，匹配前端 chat-websocket.js 中的 /ws-chat/websocket
        registry.addEndpoint("/ws-chat/websocket")
                .setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS)
                .addInterceptors(handshakeInterceptor);

        // SockJS fallback 端点；如果前端改用 SockJS 客户端，可以连接 /ws-chat
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS)
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
    }
}
