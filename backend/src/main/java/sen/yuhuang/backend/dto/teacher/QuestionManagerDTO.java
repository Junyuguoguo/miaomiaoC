package sen.yuhuang.backend.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.stylesheets.LinkStyle;
import sen.yuhuang.backend.entity.SampleQuestion;
import sen.yuhuang.backend.entity.TestSample;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionManagerDTO {
    private String id;
    private String questionName;
    private String questionDesc;
    private String bankId;
    private String examId;
    private String bankTitle;
    private String type;
    private String fullScore;
    private String level;
    private String hint;
    private String inputFormat;
    private String outputFormat;
    private List<TestSample> testSampleList;
    private List<SampleQuestion> sampleQuestionList;
    private String answerCode;
    private LocalDateTime createTime;
}
