package sen.yuhuang.backend.controller.teacher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.Question;
import sen.yuhuang.backend.service.QuestionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/")
public class QuestionManagerController {
    @Autowired
    private QuestionService questionService;

    @PostMapping("/loadQuestionList")
    public Result loadQuestionList(){
        return questionService.loadQuestionList();
    }

    @PostMapping("/addQuestion")
    public Result addQuestion(@RequestBody Map<String,Object> request){
        // 安全获取 questionName（避免 get() 返回 null 时调用 toString() 空指针）
        String questionName = request.get("questionName") != null ? request.get("questionName").toString() : null;
        String questionDesc = request.get("questionDesc") != null ? request.get("questionDesc").toString() : null;
        String fullScore = request.get("fullScore") != null ? request.get("fullScore").toString() : null;
        String level = request.get("level") != null ? request.get("level").toString() : null;
        String hint = request.get("hint") != null ? request.get("hint").toString() : null;
        String inputFormat = request.get("inputFormat") != null ? request.get("inputFormat").toString() : null;
        String outputFormat = request.get("outputFormat") != null ?  request.get("outputFormat").toString() : null;
        String answer = request.get("answer") != null ? request.get("answer").toString() : null;
        String bankId = request.get("bankId") != null ? request.get("bankId").toString() : null;
        String examId = request.get("examId") != null ? request.get("examId").toString() : null;
        String samples = request.get("samples") != null ? request.get("samples").toString() : null;
        String testSamples = request.get("testCasesJson") != null ? request.get("testCasesJson").toString() : null;

        questionName = questionName != null ? questionName.trim() : null;
        questionDesc = questionDesc != null ? questionDesc.trim() : null;
        fullScore = fullScore != null ? fullScore.trim() : null;
        level = level != null ? level.trim() : null;
        inputFormat = inputFormat != null ? inputFormat.trim() : null;
        outputFormat = outputFormat != null ? outputFormat.trim() : null;
        bankId = bankId != null ? bankId.trim() : null;
        examId = examId != null ? examId.trim() : null;

        if (questionName == null || questionName.isEmpty() ||
        questionDesc == null || questionDesc.isEmpty() ||
        fullScore == null || fullScore.isEmpty() ||
        level == null || level.isEmpty() ||
        inputFormat == null || inputFormat.isEmpty() ||
        outputFormat == null || outputFormat.isEmpty())
            return Result.error("新增问题关键参数不可为空!");

        if ((bankId == null || bankId.isEmpty()) && (examId == null || examId.isEmpty()))
            return Result.error("examId与bankId不可同时为空！");

        if (bankId != null && bankId.isEmpty()) {
            bankId = null;
        }
        if (examId != null && examId.isEmpty()) {
            examId = null;
        }

        Question question = new Question();
        question.setQuestionName(questionName);
        question.setQuestionDesc(questionDesc);
        question.setHint(hint);
        question.setInputFormat(inputFormat);
        question.setOutputFormat(outputFormat);
        question.setFullScore(BigDecimal.valueOf(Float.parseFloat(fullScore)));
        question.setLevel(Integer.valueOf(level));
        question.setQuestionCode(answer);

        if((bankId != null) && (examId == null)){
            question.setSimulationExamId(null);
            question.setBankId(Long.valueOf(bankId));
            question.setSourceType(2);
        }else if((bankId == null) && (examId != null)){
            question.setSimulationExamId(Long.valueOf(examId));
            question.setBankId(null);
            question.setSourceType(1);
        }
        question.setParticipantCount(0);
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());

        return questionService.addQuestion(question,samples,testSamples);
    }

    @PostMapping("/updateQuestion")
    public Result updateQuestion(@RequestBody Map<String, Object> request) {
        try {
            // 1. 参数提取 - ID必须存在
            String idStr = request.get("id") != null ? request.get("id").toString() : null;
            if (idStr == null || idStr.trim().isEmpty()) {
                return Result.error("题目ID不能为空");
            }

            // 2. 构建Question对象（只构建传入的非空字段）
            Question question = buildQuestionFromRequest(request);
            question.setId(Long.parseLong(idStr));

            // 3. 调用Service层（传递完整的request用于处理samples和testCasesJson）
            return questionService.updateQuestion(question, request);

        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 从请求中构建Question对象（只构建传入的非空字段）
     */
    private Question buildQuestionFromRequest(Map<String, Object> request) {
        Question question = new Question();

        // 题目名称（可选）
        if (request.containsKey("questionName") && request.get("questionName") != null) {
            String questionName = request.get("questionName").toString();
            if (!questionName.trim().isEmpty()) {
                question.setQuestionName(questionName.trim());
            }
        }

        // 题目描述（可选）
        if (request.containsKey("questionDesc") && request.get("questionDesc") != null) {
            String questionDesc = request.get("questionDesc").toString();
            if (!questionDesc.trim().isEmpty()) {
                question.setQuestionDesc(questionDesc.trim());
            }
        }

        // 满分分值（可选）
        if (request.containsKey("fullScore") && request.get("fullScore") != null) {
            String fullScore = request.get("fullScore").toString();
            if (!fullScore.trim().isEmpty()) {
                try {
                    double score = Double.parseDouble(fullScore);
                    if (score < 0) {
                        throw new IllegalArgumentException("满分分值必须大于等于0");
                    }
                    question.setFullScore(new BigDecimal(fullScore));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("满分分值格式错误，请输入数字");
                }
            }
        }

        // 提示（可选）
        if (request.containsKey("hint") && request.get("hint") != null) {
            String hint = request.get("hint").toString();
            question.setHint(hint);
        }

        // 输入格式（可选）
        if (request.containsKey("inputFormat") && request.get("inputFormat") != null) {
            String inputFormat = request.get("inputFormat").toString();
            question.setInputFormat(inputFormat);
        }

        // 输出格式（可选）
        if (request.containsKey("outputFormat") && request.get("outputFormat") != null) {
            String outputFormat = request.get("outputFormat").toString();
            question.setOutputFormat(outputFormat);
        }

        // 参考答案（可选）
        if (request.containsKey("answer") && request.get("answer") != null) {
            String answer = request.get("answer").toString();
            question.setQuestionCode(answer);
        }

        // 难度（可选）
        if (request.containsKey("level") && request.get("level") != null) {
            String level = request.get("level").toString();
            if (!level.trim().isEmpty()) {
                try {
                    int levelValue = Integer.parseInt(level);
                    if (levelValue < 0 || levelValue > 3) {
                        throw new IllegalArgumentException("题目难度必须为0-3之间的整数");
                    }
                    question.setLevel(levelValue);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("题目难度格式错误");
                }
            }
        }

        // 题库ID（可选）
        if (request.containsKey("bankId") && request.get("bankId") != null) {
            String bankId = request.get("bankId").toString();
            if (!bankId.trim().isEmpty()) {
                question.setBankId(Long.parseLong(bankId));
            }
        }

        // 考试ID（可选）
        if (request.containsKey("examId") && request.get("examId") != null) {
            String examId = request.get("examId").toString();
            if (!examId.trim().isEmpty()) {
                question.setSimulationExamId(Long.parseLong(examId));
            }
        }

        return question;
    }

    @PostMapping("/deleteQ")
    public Result deleteQuestion(@RequestBody Map<String, Object> request) {
        String questionId = request.get("id") != null ? request.get("id").toString() : null;
        if (questionId == null || questionId.trim().isEmpty()) {
            return Result.error("参数不可为空！");
        }
        return questionService.deleteQ(questionId);
    }
}
