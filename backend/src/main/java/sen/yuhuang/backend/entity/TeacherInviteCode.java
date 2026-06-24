package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "teacher_invite_code")
public class TeacherInviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 32, unique = true, nullable = false)
    private String code;

    @Column(name = "college", length = 100, nullable = false)
    private String college;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "used_by")
    private Long usedBy;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0=未使用, 1=已使用, 2=已作废

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
}
