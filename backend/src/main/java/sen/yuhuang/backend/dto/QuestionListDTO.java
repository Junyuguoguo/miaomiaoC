package sen.yuhuang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
// 题库中题目列表的DTO
public class QuestionListDTO {
    private Long questionId;
    private String questionDesc;
    private Integer level;
    private boolean isCollected;
    private boolean isWrong;
    private Integer participantCount;
    private BigDecimal AC;
}
