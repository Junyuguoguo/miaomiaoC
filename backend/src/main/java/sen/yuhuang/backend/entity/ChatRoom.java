package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 聊天房间/频道实体（用于群聊）
 */
@Data
@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 房间名称
     */
    @Column(name = "room_name", length = 100, nullable = false)
    private String roomName;

    /**
     * 房间描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 房间类型：PUBLIC-公开房间, PRIVATE-私有房间
     */
    @Column(name = "room_type", length = 20, nullable = false)
    private String roomType = "PUBLIC";

    /**
     * 创建者用户ID
     */
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    /**
     * 最大成员数（0表示无限制）
     */
    @Column(name = "max_members")
    private Integer maxMembers = 0;

    /**
     * 当前成员数
     */
    @Column(name = "current_members")
    private Integer currentMembers = 0;

    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 学院归属（NULL表示全校可见）
     */
    @Column(name = "college", length = 100)
    private String college;

    /**
     * 6位群号（自动生成）
     */
    @Column(name = "group_number", length = 6, unique = true)
    private String groupNumber;

    /**
     * 房间等级：FREE-免费普通房间, VIP-VIP专属房间
     */
    @Column(name = "room_level", length = 10)
    private String roomLevel = "FREE";

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 群公告内容
     */
    @Column(name = "notice", length = 1000)
    private String notice;

    /**
     * 公告更新时间
     */
    @Column(name = "notice_updated_at")
    private LocalDateTime noticeUpdatedAt;
}
