package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户做题记录实体类
 * 对应数据库表：user_question_record
 */
@Data
@Entity
@Table(name = "user_question_record") // 核心：修改表名映射
@AllArgsConstructor
@NoArgsConstructor
public class UserQuestionRecord { // 类名同步改为UserQuestionRecord

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
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 题目ID（外键）
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 该题得分（默认值：0.00）
     */
    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "code", columnDefinition = "LONGTEXT")
    private String code;
    /**
     * 答题时间（不可更新）
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间（自动更新）
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 实体持久化前自动设置创建时间和初始更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now; // 初始化更新时间为创建时间
        // 确保得分默认值为0.00
        if (score == null) {
            score = BigDecimal.ZERO;
        }
    }

    /**
     * 实体更新前自动更新updated_at字段
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}