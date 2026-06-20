package sen.yuhuang.backend.common.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import sen.yuhuang.backend.common.utils.RedisUtil;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.repository.UserRepository;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 
 * 在 WebSocket 握手阶段提取用户信息并存储到会话中
 * 通过查询参数传递 token 和 userId，在服务端验证 token 有效性
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final RedisUtil redisUtil;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, 
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // 从查询参数中提取 token 和 userId
        String token = getQueryParam(request, "token");
        String userIdStr = getQueryParam(request, "userId");
        
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket 握手失败：缺少 token 参数");
            return false;
        }
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            log.warn("WebSocket 握手失败：缺少 userId 参数");
            return false;
        }
        
        try {
            Long userId = Long.parseLong(userIdStr);
            
            // 查找用户以获取用户名
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("WebSocket 握手失败：用户不存在, userId={}", userId);
                return false;
            }
            
            // 验证 token 是否有效
            boolean isValid = redisUtil.validateToken(user.getUsername(), token);
            if (!isValid) {
                log.warn("WebSocket 握手失败：token 验证失败, userId={}", userId);
                return false;
            }
            
            // 将用户ID存储到 WebSocket 会话属性中
            attributes.put("userId", userId.toString());
            log.info("WebSocket 握手成功，用户: {} (ID={})", user.getUsername(), userId);
            return true;
            
        } catch (NumberFormatException e) {
            log.warn("WebSocket 握手失败：无效的用户ID格式: {}", userIdStr);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 握手完成后的处理（可选）
    }

    /**
     * 从查询参数中提取指定参数值
     */
    private String getQueryParam(ServerHttpRequest request, String paramName) {
        String query = request.getURI().getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }
        
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }
}
