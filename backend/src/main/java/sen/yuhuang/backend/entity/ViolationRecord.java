package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 考试违规记录实体类
 * 对应数据库表：violation_record
 */
@Data
@Entity
@Table(name = "violation_record")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ViolationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 违规记录ID

    @Column(name = "exam_id", nullable = false)
    private Long examId; // 关联考试ID

    @Column(name = "user_id", nullable = false)
    private Long userId; // 关联用户ID

    @Column(name = "violation_desc", nullable = false, columnDefinition = "TEXT")
    private String violationDesc; // 违规说明

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime; // 创建时间

    @Column(name = "operator_id")
    private Long operatorId; // 创建人ID（可选）

    @Column(name = "operator_name", length = 50)
    private String operatorName; // 创建人姓名（可选）

    // 自动填充创建时间
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}