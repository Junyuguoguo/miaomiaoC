package sen.yuhuang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sen.yuhuang.backend.entity.SampleQuestion;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeQuestionDTO {
    private BigDecimal fullScore;
    private String questionDesc;
    private String hint;
    private String inputFormat;
    private String outputFormat;
    private List<SampleQuestion>  sampleQuestions;
}
