package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 聊天房间成员关系表
 */
@Data
@Entity
@Table(name = "chat_room_member")
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 房间ID
     */
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 角色：ADMIN-管理员, MEMBER-普通成员
     */
    @Column(name = "role", length = 20, nullable = false)
    private String role = "MEMBER";

    /**
     * 加入时间
     */
    @CreationTimestamp
    @Column(name = "join_time", nullable = false, updatable = false)
    private LocalDateTime joinTime;

    /**
     * 最后阅读消息时间（用于未读消息统计）
     */
    @Column(name = "last_read_time")
    private LocalDateTime lastReadTime;
}
