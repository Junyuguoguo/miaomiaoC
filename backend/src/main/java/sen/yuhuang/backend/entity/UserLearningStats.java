package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户学习统计实体类
 * 对应数据库表：user_learning_stats
 */
@Data
@Entity
@Table(name = "user_learning_stats")
@AllArgsConstructor
@NoArgsConstructor
public class UserLearningStats {

    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户ID（外键）
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 已考次数（默认值：0）
     */
    @Column(name = "exam_count", nullable = false)
    private Integer examCount = 0;

    /**
     * 题目通过率（百分比，0-100，默认值：0.00）
     */
    @Column(name = "question_pass_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal questionPassRate = BigDecimal.ZERO;

    /**
     * 做题数量（默认值：0）
     */
    @Column(name = "question_count", nullable = false)
    private Integer questionCount = 0;

    /**
     * 笔记数量（默认值：0）
     */
    @Column(name = "note_count", nullable = false)
    private Integer noteCount = 0;


    @Column(name = "success_count", nullable = false)
    private Integer successCount = 0;
    /**
     * 统计更新时间（自动更新）
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 实体持久化前自动设置初始更新时间和默认值
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        updatedAt = now;
        // 确保数值型字段默认值正确
        if (examCount == null) examCount = 0;
        if (questionPassRate == null) questionPassRate = BigDecimal.ZERO;
        if (questionCount == null) questionCount = 0;
        if (noteCount == null) noteCount = 0;
    }

    /**
     * 实体更新前自动更新updated_at字段
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}