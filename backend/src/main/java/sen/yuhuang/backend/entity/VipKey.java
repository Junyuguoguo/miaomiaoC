package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "vip_key")
@AllArgsConstructor
@NoArgsConstructor
public class VipKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "key_code", nullable = false, unique = true, length = 64)
    private String keyCode;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "used_by")
    private Long usedBy;

    @Column(name = "used_time")
    private LocalDateTime usedTime;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "note", length = 200)
    private String note;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
