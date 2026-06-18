package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "interview_question_tag_rel")
@AllArgsConstructor
@NoArgsConstructor
public class InterviewQuestionTagRel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
