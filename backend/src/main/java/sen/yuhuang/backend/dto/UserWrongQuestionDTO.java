package sen.yuhuang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sen.yuhuang.backend.entity.SampleQuestion;
import sen.yuhuang.backend.entity.TestSampleResult;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWrongQuestionDTO {
    private Long id;                    // 错题记录ID
    private Long questionId;            // 题目ID
    private Long questionBankId;        // 题库ID
    private String bankTitle;           // 题库名称
    private String questionDesc;        // 题目描述
    private String level;               // 难度等级
    private String inputFormat;
    private String outputFormat;
    private String hint;
    private String userAnswer;          // 用户答案
    private String correctAnswer;       // 正确答案
    private String code;
    private List<SampleQuestion> sampleQuestionList;//题的样例
    private List<TestSampleResultsDto> testSampleResultList;// 题的测试用例结果
    private String errorMessage;        // 错误信息
    private LocalDateTime lastWrongTime;         // 最近错误时间
    private Integer status;             // 状态
}
