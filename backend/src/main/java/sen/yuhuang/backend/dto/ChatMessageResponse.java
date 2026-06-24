package sen.yuhuang.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息响应DTO
 */
@Data
public class ChatMessageResponse {

    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String senderCollege;
    private Long senderRole;
    private Long receiverId;
    private Long roomId;
    private String messageType;
    private String content;
    private String fileName;
    private Long fileSize;
    private String codeContent;
    private Boolean isRead;
    private Boolean recalled;
    private LocalDateTime createTime;
}
