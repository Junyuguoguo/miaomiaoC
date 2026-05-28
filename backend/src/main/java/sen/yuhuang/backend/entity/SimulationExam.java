package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模拟考试实体类
 * 对应数据库表：simulation_exam
 */
@Data
@Entity
@Table(name = "simulation_exam")
@AllArgsConstructor
@NoArgsConstructor
public class SimulationExam {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 考试题目/考试名称
     */
    @Column(name = "exam_title", nullable = false, length = 255)
    private String examTitle;

    /**
     * 考试开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 题目数量（默认0）
     */
    @Column(name = "question_count", nullable = false, columnDefinition = "int default 0")
    private Integer questionCount = 0;

    /**
     * 考试时长（单位：分钟）
     */
    @Column(name = "exam_duration", nullable = false)
    private Integer examDuration;

    /**
     * 考试须知（支持长文本）
     */
    @Column(name = "exam_notice", columnDefinition = "text")
    private String examNotice;

    /**
     * 参与人数（初始0，后续更新）
     */
    @Column(name = "participant_count", nullable = false, columnDefinition = "int default 0")
    private Integer participantCount = 0;

    /**
     * 考试状态：0-未开始，1-进行中，2-已结束（默认0）
     */
    @Column(name = "exam_status", nullable = false, columnDefinition = "tinyint default 0")
    private Integer examStatus = 0;

    /**
     * 是否仅会员可参与：0-所有人，1-仅会员（默认0）
     */
    @Column(name = "is_vip_only", nullable = false, columnDefinition = "tinyint(1) default 0")
    private Integer isVipOnly = 0;

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
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    /**
     * 实体更新前自动更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    @Column(name = "question_source", updatable = false)
    private String questionSource;
}