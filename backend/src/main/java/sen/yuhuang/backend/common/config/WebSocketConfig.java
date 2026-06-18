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
        // 注册 WebSocket 端点，客户端通过这个端点建立连接
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("*")  // 允许所有来源（生产环境应该限制）
                .addInterceptors(handshakeInterceptor)  // 添加握手拦截器用于身份验证
                .withSockJS();  // 启用 SockJS  fallback
        
        // 也可以添加原生 WebSocket 端点（不使用 SockJS）
        // registry.addEndpoint("/ws-chat").setAllowedOrigins("*");
    }
}
