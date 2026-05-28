package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_exam_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationExamResult {

    /**
     * 考试记录主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 考试ID（外键关联模拟考试表）
     */
    @Column(name = "exam_id", nullable = false)
    private Long examId;

    /**
     * 用户ID（外键关联用户表）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 考试总分（保留2位小数）
     */
    @Column(name = "exam_total_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal examTotalScore = BigDecimal.ZERO;

    /**
     * 是否通过：0-未通过 1-通过
     */
    @Column(name = "is_passed", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isPassed = 0;

    /**
     * 考试提交时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @Column(name = "is_deleted", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDeleted = 0;

    // ========== 辅助方法 ==========

    /**
     * 判断考试是否通过
     */
    public boolean isPassed() {
        return isPassed != null && isPassed == 1;
    }

    /**
     * 判断记录是否被逻辑删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }

    /**
     * 设置考试是否通过（简化赋值）
     */
    public void setPassed(boolean passed) {
        this.isPassed = passed ? 1 : 0;
    }
}