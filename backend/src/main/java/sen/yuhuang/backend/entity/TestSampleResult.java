package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 编程题测试用例结果实体类
 * 对应数据库表：test_sample_result
 */
@Data
@Entity
@Table(name = "test_sample_result")
@AllArgsConstructor
@NoArgsConstructor
public class TestSampleResult {

    /**
     * 测试用例结果主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户ID（外键关联用户表）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 考试ID（外键关联模拟考试表）
     */
    @Column(name = "exam_id", nullable = false)
    private Long examId;

    /**
     * 题目ID（外键关联题目表）
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 用例ID（外键关联用例表）
     */
    @Column(name = "test_sample_id", nullable = false)
    private Long testSampleId;


    /**
     * 测试结果是否通过：0-未通过 1-通过
     */
    @Column(name = "is_passed", nullable = false, columnDefinition = "TINYINT")
    private Integer isPassed;

    /**
     * 测试报错信息（编译/运行错误，无错误则为空）
     */
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    /**
     * 用时（单位：毫秒ms）
     */
    @Column(name = "used_time", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer usedTime;


    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @Column(name = "is_deleted", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDeleted;

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
}