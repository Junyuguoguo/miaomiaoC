package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户错题本实体类
 * 对应数据库表：user_wrong_question
 */
@Data
@Entity
@Table(name = "user_wrong_question",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_bank_id", columnList = "question_bank_id"),
                @Index(name = "idx_question_id", columnList = "question_id")
        })
@AllArgsConstructor
@NoArgsConstructor
public class UserWrongQuestion {

    /**
     * 错题ID（主键，自增）
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
     * 所属题库ID（外键）
     */
    @Column(name = "question_bank_id", nullable = false)
    private Long questionBankId;

    /**
     * 用户答案
     */
    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    /**
     * 正确答案
     */
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "error_info")
    private String errorInfo;

    /**
     * 最近错误时间
     */
    @Column(name = "last_wrong_time")
    private LocalDateTime lastWrongTime;

    /**
     * 首次错误时间
     */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 实体持久化前自动设置创建时间、最近错误时间和初始更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        lastWrongTime = now;
        updateTime = now;
    }

    /**
     * 实体更新前自动更新update_time字段
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 可选：关联题目实体（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    /**
     * 可选：关联题库实体（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id", insertable = false, updatable = false)
    private QuestionBank questionBank;

    /**
     * 可选：关联用户实体（懒加载）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 状态常量
     */
    public static class Status {
        public static final int MASTERED = 0;      // 已掌握
        public static final int NOT_MASTERED = 1;  // 未掌握
    }
}