package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sen.yuhuang.backend.dto.TestSampleResultsDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目实体类
 * 对应数据库表：question
 */
@Data
@Entity
@Table(name = "question")
@AllArgsConstructor
@NoArgsConstructor
public class Question {

    /**
     * 题目ID（主键，自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 关联考试ID（外键）
     */
    @Column(name = "simulation_exam_id", nullable = false)
    private Long simulationExamId;

    /**
     * 题目描述
     */
    @Column(name = "question_desc", nullable = false, columnDefinition = "TEXT")
    private String questionDesc;

    @Column(name = "question_name")
    private String questionName;

    @Column(name = "question_code")
    private String questionCode;

    /**
     * 提示（默认值：暂无提示）
     */
    @Column(name = "hint", length = 500)
    private String hint;

    @Column(name = "bank_id")
    private Long bankId;

    @Column(name = "source_type")
    private Integer sourceType;

    @Column(name = "level")
    private Integer level;

    /**
     * 输入格式
     */
    @Column(name = "input_format", nullable = false, columnDefinition = "TEXT")
    private String inputFormat;

    /**
     * 输出格式
     */
    @Column(name = "output_format", nullable = false, columnDefinition = "TEXT")
    private String outputFormat;

    /**
     * 满分分值
     */
    @Column(name = "full_score", nullable = false, precision = 5, scale = 1)
    private BigDecimal fullScore;

    /**
     * 创建时间（不可更新）
     */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动更新）
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "participant_count")
    private Integer participantCount;

    /**
     * 实体持久化前自动设置创建时间和初始更新时间
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now; // 初始化更新时间为创建时间
    }

    /**
     * 实体更新前自动更新update_time字段
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "questionId") // mappedBy指向SampleQuestion中的questionId字段
    List<SampleQuestion> sampleQuestions;

}