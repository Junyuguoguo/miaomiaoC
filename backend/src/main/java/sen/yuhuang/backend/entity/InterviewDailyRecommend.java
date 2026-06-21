package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interview_daily_recommend")
@AllArgsConstructor
@NoArgsConstructor
public class InterviewDailyRecommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "recommend_date", nullable = false)
    private LocalDate recommendDate;

    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;

    @Column(name = "is_auto", nullable = false, columnDefinition = "tinyint default 1")
    private Integer isAuto = 1;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (sortOrder == null) sortOrder = 0;
        if (isAuto == null) isAuto = 1;
    }
}
