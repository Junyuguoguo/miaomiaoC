package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题库实体类
 * 对应数据库表：question_bank
 */
@Data
@Entity
@Table(name = "question_bank")
@AllArgsConstructor
@NoArgsConstructor
public class QuestionBank {

    /**
     * 题库ID（主键，自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 题库标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 题库描述
     */
    @Column(name = "desc", length = 500)
    private String desc;

    /**
     * 难度等级：1-入门，2-简单，3-中等，4-困难
     */
    @Column(name = "level")
    private Integer level;

    /**
     * 题目数量
     */
    @Column(name = "question_count")
    private Integer questionCount;

    /**
     * 学习人数
     */
    @Column(name = "view_count")
    private Integer viewCount;

    /**
     * 推荐度（0-100）
     */
    @Column(name = "recommend")
    private Integer recommend;

    /**
     * 是否为VIP题库：0-普通题库，1-VIP专属题库
     */
    @Column(name = "is_vip", nullable = false)
    private Integer isVip;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
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
        updateTime = now;
    }

    /**
     * 实体更新前自动更新update_time字段
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}