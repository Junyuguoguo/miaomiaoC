package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_room_setting", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "room_id"}))
public class UserRoomSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    @Column(name = "is_muted")
    private Boolean isMuted = false;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() { this.createTime = LocalDateTime.now(); }
}
