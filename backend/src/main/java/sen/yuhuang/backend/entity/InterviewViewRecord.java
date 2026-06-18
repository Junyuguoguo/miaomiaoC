package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interview_view_record")
@AllArgsConstructor
@NoArgsConstructor
public class InterviewViewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "view_time")
    private LocalDateTime viewTime;

    @PrePersist
    protected void onCreate() {
        viewTime = LocalDateTime.now();
    }
}
