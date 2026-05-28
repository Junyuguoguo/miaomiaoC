package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 题目样例实体类
 * 对应数据库表：sample_question
 */
@Data
@Entity
@Table(name = "sample_question")
@AllArgsConstructor
@NoArgsConstructor
public class SampleQuestion {

    /**
     * 样例ID（主键，自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 关联题目ID（外键）
     */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "input")
    private String input;

    @Column(name = "output", columnDefinition = "TEXT")
    private String output;

    /**
     * 样例描述（包含输入输出示例）
     */
    @Column(name = "sample_desc", nullable = false, columnDefinition = "TEXT")
    private String sampleDesc;

}