package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发送者用户ID
     */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /**
     * 发送者用户名（冗余字段，方便查询）
     */
    @Column(name = "sender_name", length = 50)
    private String senderName;

    /**
     * 接收者用户ID（单聊时填写，群聊时为null）
     */
    @Column(name = "receiver_id")
    private Long receiverId;

    /**
     * 房间/频道ID（群聊时填写，单聊时为null）
     */
    @Column(name = "room_id")
    private Long roomId;

    /**
     * 消息类型：TEXT-文本, IMAGE-图片, FILE-文件, SYSTEM-系统消息
     */
    @Column(name = "message_type", length = 20, nullable = false)
    private String messageType = "TEXT";

    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 是否已读
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
