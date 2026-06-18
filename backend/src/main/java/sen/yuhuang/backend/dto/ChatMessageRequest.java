package sen.yuhuang.backend.dto;

import lombok.Data;

/**
 * 发送聊天消息请求DTO
 */
@Data
public class ChatMessageRequest {

    /**
     * 接收者用户ID（单聊时必填）
     */
    private Long receiverId;

    /**
     * 房间ID（群聊时必填）
     */
    private Long roomId;

    /**
     * 消息类型：TEXT, IMAGE, FILE, SYSTEM
     */
    private String messageType = "TEXT";

    /**
     * 消息内容
     */
    private String content;
}
