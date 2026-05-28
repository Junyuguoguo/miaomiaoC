package sen.yuhuang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionDto {
    private String questionDesc;
    private String hint;
    private String sore;
    private String code;
    private String fullScore;
    private String inputFormat;
    private String outputFormat;
    private List<TestSampleResultsDto> testSampleResultsDtos;
}
