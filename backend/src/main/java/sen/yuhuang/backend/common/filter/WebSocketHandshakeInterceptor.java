package sen.yuhuang.backend.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 
 * 在 WebSocket 握手阶段提取用户信息并存储到会话中
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, 
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // 从请求头或参数中获取 Token
        String token = extractToken(request);
        
        if (token != null && !token.isEmpty()) {
            // TODO: 这里应该解析 Token 获取用户ID
            // 简化处理：假设 Token 格式为 "Bearer userId"
            try {
                if (token.startsWith("Bearer ")) {
                    String userIdStr = token.substring(7);
                    Long userId = Long.parseLong(userIdStr);
                    
                    // 将用户ID存储到 WebSocket 会话属性中
                    attributes.put("userId", userId.toString());
                    log.info("WebSocket 握手成功，用户ID: {}", userId);
                    return true;
                }
            } catch (NumberFormatException e) {
                log.warn("无效的用户ID格式: {}", token);
            }
        }
        
        log.warn("WebSocket 握手失败：未找到有效的 Token");
        return false;  // 拒绝握手
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 握手完成后的处理（可选）
    }

    /**
     * 从请求中提取 Token
     */
    private String extractToken(ServerHttpRequest request) {
        // 优先从查询参数获取
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }
        
        // 其次从请求头获取
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader;
        }
        
        return null;
    }
}
