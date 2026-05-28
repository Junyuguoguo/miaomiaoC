package sen.yuhuang.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestSampleResultsDto {
    private String name;
    private String isPassed;
    private String usedTime;
    private String errorMessage;
    private String input;
    private String output;
    private String actualOutput;
}
