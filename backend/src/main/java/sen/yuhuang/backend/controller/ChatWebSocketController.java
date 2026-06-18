package sen.yuhuang.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.ChatMessageRequest;
import sen.yuhuang.backend.dto.ChatMessageResponse;
import sen.yuhuang.backend.service.ChatMessageService;

/**
 * WebSocket 聊天控制器
 * 
 * 处理实时消息的发送和接收
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理单聊消息发送
     * 
     * 客户端发送: /app/chat/private
     * 服务端广播: /user/{receiverId}/queue/messages
     */
    @MessageMapping("/chat/private")
    public void sendPrivateMessage(@Payload ChatMessageRequest request, 
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取发送者ID（需要在连接时设置）
            String senderIdStr = (String) headerAccessor.getSessionAttributes().get("userId");
            if (senderIdStr == null) {
                log.warn("WebSocket 会话中未找到用户ID");
                return;
            }
            
            Long senderId = Long.parseLong(senderIdStr);

            // 保存消息到数据库
            ChatMessageResponse response = chatMessageService.sendPrivateMessage(senderId, request);

            // 向接收者推送消息
            String destination = "/user/" + request.getReceiverId() + "/queue/messages";
            messagingTemplate.convertAndSend(destination, Result.success(response));

            // 同时发送给发送者（用于确认消息已发送）
            String senderDestination = "/user/" + senderId + "/queue/messages";
            messagingTemplate.convertAndSend(senderDestination, Result.success(response));

            log.info("单聊消息发送成功: {} -> {}, 消息ID: {}", senderId, request.getReceiverId(), response.getId());

        } catch (Exception e) {
            log.error("发送单聊消息失败", e);
            // 发送错误消息给客户端
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getSessionAttributes().get("userId").toString(),
                "/queue/errors",
                Result.error("消息发送失败: " + e.getMessage())
            );
        }
    }

    /**
     * 处理群聊消息发送
     * 
     * 客户端发送: /app/chat/room
     * 服务端广播: /topic/room/{roomId}
     */
    @MessageMapping("/chat/room")
    public void sendRoomMessage(@Payload ChatMessageRequest request,
                                SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 从会话中获取发送者ID
            String senderIdStr = (String) headerAccessor.getSessionAttributes().get("userId");
            if (senderIdStr == null) {
                log.warn("WebSocket 会话中未找到用户ID");
                return;
            }
            
            Long senderId = Long.parseLong(senderIdStr);

            // 保存消息到数据库
            ChatMessageResponse response = chatMessageService.sendRoomMessage(senderId, request);

            // 向房间内所有成员推送消息
            String destination = "/topic/room/" + request.getRoomId();
            messagingTemplate.convertAndSend(destination, Result.success(response));

            log.info("群聊消息发送成功: 房间={}, 消息ID={}", request.getRoomId(), response.getId());

        } catch (Exception e) {
            log.error("发送群聊消息失败", e);
            // 发送错误消息给客户端
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getSessionAttributes().get("userId").toString(),
                "/queue/errors",
                Result.error("消息发送失败: " + e.getMessage())
            );
        }
    }

    /**
     * 标记消息为已读
     * 
     * 客户端发送: /app/chat/read
     */
    @MessageMapping("/chat/read")
    public void markAsRead(@Payload java.util.List<Long> messageIds,
                          SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userIdStr = (String) headerAccessor.getSessionAttributes().get("userId");
            if (userIdStr == null) {
                return;
            }
            
            Long userId = Long.parseLong(userIdStr);
            chatMessageService.markAsRead(userId, messageIds);
            
            log.info("标记消息已读: 用户={}, 消息数={}", userId, messageIds.size());
        } catch (Exception e) {
            log.error("标记消息已读失败", e);
        }
    }
}
