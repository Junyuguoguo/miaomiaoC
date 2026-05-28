package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.QuestionDto;
import sen.yuhuang.backend.dto.TestSampleResultsDto;
import sen.yuhuang.backend.entity.*;
import sen.yuhuang.backend.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
public class SimulationExamResultService {
    @Autowired
    SimulationExamRepository simulationExamRepository;

    @Autowired
    SimulationExamResultRepository simulationExamResultRepository;

    @Autowired
    QuestionRepository  questionRepository;

    @Autowired
    TestSampleResultRepository testSampleResultRepository;

    @Autowired
    TestSampleRepository testSampleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserQuestionRecordRepository userQuestionRecordRepository;

    public Result getExamRecordList(String userId) {
        try{
            Long userIdLong = Long.parseLong(userId);
            // 1.确定userID的存在
            User user = userRepository.findUserByUserId(userIdLong);
            if (user == null) return Result.error("加载考试记录，User不存在！");

            ArrayList<HashMap<String, Object>> data = new ArrayList<>();
            // 2.查询考试记录
            List<SimulationExamResult> records = simulationExamResultRepository.
                    findSimulationExamResultByUserId(userIdLong);
            if (records.size() <= 0)
                return Result.ok(data).setMessage("考试记录为空！");

            // 3.查询该考试基本信息
            for (SimulationExamResult simulationExamResult : records) {
                HashMap<String, Object> map = new HashMap<>();
                SimulationExam exam = simulationExamRepository.getExamByExamId(simulationExamResult.getExamId());
                map.put("examName",exam.getExamTitle());
                map.put("examTime",simulationExamResult.getUpdateTime());
                map.put("score",simulationExamResult.getExamTotalScore());
                map.put("status",simulationExamResult.getIsPassed());
                // 封装考试题目
                List<Question> questions = questionRepository.findQuestionBySimulationExamIdAndType(simulationExamResult.getExamId(),1);
                ArrayList<QuestionDto> questionDtos = new ArrayList<>();

                // 封装每个题目的用例测试情况
                for (Question question : questions) {
                    //封装QuestionDTO
                    QuestionDto questionDto = new QuestionDto();
                    questionDto.setHint(question.getHint());
                    questionDto.setQuestionDesc(question.getQuestionDesc());
                    // 封装该题得分
                    UserQuestionRecord record = userQuestionRecordRepository.
                            findQuestionRecordByUserIdAndQuestionId(userIdLong,question.getId());
                    questionDto.setSore(record.getScore().toString());
                    questionDto.setInputFormat(question.getInputFormat());
                    questionDto.setOutputFormat(question.getOutputFormat());
                    questionDto.setFullScore(question.getFullScore().toString());
                    questionDto.setCode(record.getCode());

                    //封装每个问题的测试用例实际情况
                    List<TestSampleResult> testResults = testSampleResultRepository.
                            findTestSampleResultByUserIdAndQuestionId(userIdLong, question.getId());
                    ArrayList<TestSampleResultsDto> testDto = new ArrayList<>();
                    for (TestSampleResult testSampleResult : testResults) {
                        // 查询测试用例输入和输出
                        TestSample sample = testSampleRepository.
                                findTestSampleById(testSampleResult.getTestSampleId());

                        TestSampleResultsDto dto = TestSampleResultsDto.builder()
                                .name("用例"+testSampleResult.getId())
                                .isPassed(testSampleResult.getIsPassed().toString())
                                .usedTime(testSampleResult.getUsedTime().toString())
                                .input(sample.getInput())
                                .output(sample.getOutput())
                                .actualOutput(testSampleResult.getActualOutput())
                                .errorMessage(testSampleResult.getErrorMsg())
                                .build();
                        testDto.add(dto);
                    }
                    questionDto.setTestSampleResultsDtos(testDto);
                    questionDtos.add(questionDto);
                }
                map.put("questions",questionDtos);
                data.add(map);
            }
            return Result.ok(data);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    public Result getExamResultById(String userId, String examId) {
        try {
            Long userIdLong = Long.parseLong(userId);
            Long examIdLong = Long.parseLong(examId);

            User user = userRepository.findUserByUserId(userIdLong);
            if (user == null) return Result.error("加载考试结果，User不存在！");

            SimulationExam exam = simulationExamRepository.getExamByExamId(examIdLong);
            if (exam == null) return Result.error("加载考试结果，考试不存在！");

            SimulationExamResult result = simulationExamResultRepository
                    .findSimulationExamResultByUserIdAndExamId(userIdLong, examIdLong);
            if (result == null) return Result.error("暂无该考试提交记录！");

            List<Question> questions = questionRepository.findQuestionBySimulationExamId(examIdLong);
            BigDecimal fullScore = BigDecimal.ZERO;
            int correctCount = 0;
            for (Question question : questions) {
                fullScore = fullScore.add(question.getFullScore() == null ? BigDecimal.ZERO : question.getFullScore());
                UserQuestionRecord record = userQuestionRecordRepository
                        .findQuestionRecordByUserIdAndQuestionId(userIdLong, question.getId());
                if (record != null
                        && question.getFullScore() != null
                        && record.getScore() != null
                        && record.getScore().compareTo(question.getFullScore()) >= 0) {
                    correctCount++;
                }
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put("examTitle", exam.getExamTitle());
            data.put("examDuration", exam.getExamDuration());
            data.put("questionCount", questions.size());
            data.put("fullScore", fullScore);
            data.put("totalScore", result.getExamTotalScore());
            data.put("isPassed", result.getIsPassed());
            data.put("correctCount", correctCount);
            data.put("submitTime", result.getUpdateTime() == null ? result.getCreateTime() : result.getUpdateTime());

            return Result.ok(data);
        } catch (NumberFormatException e) {
            return Result.error("用户ID/考试ID必须为数字");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
