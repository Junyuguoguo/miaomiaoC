package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interview_question")
@AllArgsConstructor
@NoArgsConstructor
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "analysis", columnDefinition = "TEXT")
    private String analysis;

    @Column(name = "college_id")
    private Long collegeId;

    @Column(name = "major_id")
    private Long majorId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "is_vip", nullable = false, columnDefinition = "tinyint default 0")
    private Integer isVip = 0;

    @Column(name = "is_free", nullable = false, columnDefinition = "tinyint default 0")
    private Integer isFree = 0;

    @Column(name = "is_published", nullable = false, columnDefinition = "tinyint default 1")
    private Integer isPublished = 1;

    @Column(name = "view_count", nullable = false, columnDefinition = "int default 0")
    private Integer viewCount = 0;

    @Column(name = "sort_order", nullable = false, columnDefinition = "int default 0")
    private Integer sortOrder = 0;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
        if (isVip == null) isVip = 0;
        if (isFree == null) isFree = 0;
        if (isPublished == null) isPublished = 1;
        if (viewCount == null) viewCount = 0;
        if (sortOrder == null) sortOrder = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
