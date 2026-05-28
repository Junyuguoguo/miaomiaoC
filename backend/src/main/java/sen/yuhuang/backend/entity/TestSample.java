package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 编程题测试用例实体类
 * 对应数据库表：test_sample
 */
@Data
@Entity
@Table(name = "test_sample")
@AllArgsConstructor
@NoArgsConstructor
public class TestSample {

    /**
     * 测试用例主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 关联的题目ID（外键）
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /**
     * 测试用例输入内容（如C代码scanf需要的参数，无输入则为空）
     */
    @Column(name = "input", columnDefinition = "TEXT")
    private String input;

    /**
     * 测试用例预期输出（代码运行后需匹配的结果）
     */
    @Column(name = "output", nullable = false, columnDefinition = "TEXT")
    private String output;

    /**
     * 用例执行顺序（默认1，数字越小越先执行）
     */
    @Column(name = "sort", columnDefinition = "TINYINT DEFAULT 1")
    private Integer sort;

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