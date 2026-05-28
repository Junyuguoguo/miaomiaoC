package sen.yuhuang.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.*;
import sen.yuhuang.backend.repository.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class SimulationExamService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    SimulationExamRepository simulationExamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SimulationExamResultRepository simulationExamResultRepository;

    @Autowired
    UserLearningStatsRepository  userLearningStatsRepository;

    @Autowired
    QuestionRepository  questionRepository;

    @Autowired
    TestSampleResultRepository testSampleResultRepository;

    @Autowired
    ViolationRecordRepository violationRecordRepository;

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    public Result getExamList(String roleId) {
        List<SimulationExam> simulationExams = new ArrayList<>();
        if(Long.valueOf(roleId) <= 1){
            simulationExams = simulationExamRepository.getExamList(0);
        }else {
            simulationExams = simulationExamRepository.getExamList();
        }
        return Result.ok(simulationExams);
    }

    public Result getExamByExamId(String examId) {
        try{
            SimulationExam simulationExam = simulationExamRepository.getExamByExamId(Long.valueOf(examId));
            return Result.ok(simulationExam);
        }catch (Exception e){
            e.printStackTrace();
            return Result.badRequest(e.getMessage());
        }
    }

    public Result submitExam(String examId, String userId, String scoreTotal) {
        try {
            Long userIdLong = Long.valueOf(userId);
            Long examIdLong = Long.valueOf(examId);
            Long scoreTotalLong = Long.valueOf(scoreTotal);

            // 1.查询UserId是否存在
            User user = userRepository.findUserByUserId(userIdLong);
            if (user == null) return Result.error("交卷的用户不存在！");

            // 2.查询ExamId是否存在
            SimulationExam simulationExam = simulationExamRepository.getExamByExamId(examIdLong);
            if (simulationExam == null) Result.error("交卷的考试不存在！");

            // 更新参与人数
            simulationExamRepository.
                    updateParticipantCountByExamId(examIdLong, Objects.requireNonNull(simulationExam).getParticipantCount()+1);

            // 3.是否已经存在该考试记录
            SimulationExamResult oldSimulationExamResult = simulationExamResultRepository.
                    findSimulationExamResultByUserIdAndExamId(userIdLong,examIdLong);

            // 更新用户学习统计
            UserLearningStats stats = userLearningStatsRepository.findUserLearningStatsByUserId(userIdLong);
            if (stats == null) {
                // 新增
                UserLearningStats userLearningStats = new UserLearningStats();
                userLearningStats.setUserId(userIdLong);
                userLearningStats.setExamCount(1);
                userLearningStats.setQuestionCount(0);
                userLearningStats.setSuccessCount(0);
                userLearningStats.setNoteCount(0);
                userLearningStats.setUpdatedAt(LocalDateTime.now());
                userLearningStatsRepository.save(userLearningStats);
            }else {
                // 更新
                stats.setExamCount(stats.getExamCount()+1);
                userLearningStatsRepository.updateExamCountById(
                        stats.getId(),stats.getExamCount());
            }

            // 4.生成新的考试记录
            SimulationExamResult simulationExamResult = new SimulationExamResult();
            simulationExamResult.setExamTotalScore(BigDecimal.valueOf(scoreTotalLong));
            simulationExamResult.setUserId(userIdLong);
            simulationExamResult.setExamId(examIdLong);

            // 获取是否通过考试 总分> 60%即是及格
            Long sumScore = 0L;
            List<Question> questions = questionRepository.findQuestionBySimulationExamId(examIdLong);
            for(Question question : questions){
                sumScore += Double.valueOf(question.getFullScore().toString()).longValue();
            }
            if (scoreTotalLong >= sumScore*0.6) simulationExamResult.setIsPassed(1);
            else simulationExamResult.setIsPassed(0);

            if(oldSimulationExamResult == null){
                simulationExamResult.setCreateTime(LocalDateTime.now());
                simulationExamResultRepository.save(simulationExamResult);
                return Result.ok(simulationExamResult);
            }else {
                simulationExamResult.setUpdateTime(LocalDateTime.now());
                simulationExamResultRepository.
                        updateSimulationExamResultByUserIdAndExamId(
                                oldSimulationExamResult.getId(),
                                simulationExamResult.getExamTotalScore(),
                                simulationExamResult.getIsPassed(),
                                simulationExamResult.getUpdateTime());
                return Result.ok(simulationExamResult);
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.badRequest(e.getMessage());
        }
    }

    @Transactional
    public void updateExamStatus(Long examId, Integer newStatus) {
        SimulationExam exam = simulationExamRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("考试不存在"));
        exam.setExamStatus(newStatus);
        simulationExamRepository.save(exam);
    }

    public Result loadExamData() {
        List<SimulationExam> simulationExams = simulationExamRepository.findAll();
        return Result.ok(simulationExams);
    }

    @Transactional
    public Result saveExam(Map<String, Object> request) {
        try {
            String examTitle = getString(request.get("examTitle"));
            String startTimeText = getString(request.get("startTime"));
            String endTimeText = getString(request.get("endTime"));
            String examDurationText = getString(request.get("examDuration"));
            String examStatusText = getString(request.get("examStatus"));
            String needVipText = getString(request.get("needVip"));
            String examIdText = getString(request.get("id"));

            if (examTitle == null || examTitle.isEmpty()) {
                return Result.error("考试名称不能为空");
            }
            if (startTimeText == null || startTimeText.isEmpty() || endTimeText == null || endTimeText.isEmpty()) {
                return Result.error("开始时间和结束时间不能为空");
            }
            if (examDurationText == null || examDurationText.isEmpty()) {
                return Result.error("考试时长不能为空");
            }

            LocalDateTime startTime = parseDateTime(startTimeText);
            LocalDateTime endTime = parseDateTime(endTimeText);
            if (endTime.isBefore(startTime)) {
                return Result.error("结束时间不能早于开始时间");
            }

            Integer examDuration = Integer.parseInt(examDurationText);
            Integer examStatus = examStatusText == null || examStatusText.isEmpty() ? 0 : Integer.parseInt(examStatusText);
            Integer needVip = needVipText == null || needVipText.isEmpty() ? 0 : Integer.parseInt(needVipText);

            SimulationExam simulationExam;
            if (examIdText != null && !examIdText.isEmpty()) {
                Long examId = Long.valueOf(examIdText);
                simulationExam = simulationExamRepository.findById(examId).orElse(null);
                if (simulationExam == null) {
                    return Result.error("考试不存在");
                }
            } else {
                simulationExam = new SimulationExam();
                simulationExam.setParticipantCount(0);
            }

            simulationExam.setExamTitle(examTitle);
            simulationExam.setStartTime(startTime);
            simulationExam.setEndTime(endTime);
            simulationExam.setExamDuration(examDuration);
            simulationExam.setExamStatus(examStatus);
            simulationExam.setIsVipOnly(needVip);

            SimulationExam savedExam = simulationExamRepository.save(simulationExam);

            Set<Long> selectedQuestionIds = parseQuestionIds(request.get("selectedQuestionIds"));
            List<Question> linkedQuestions = questionRepository.findQuestionBySimulationExamId(savedExam.getId());

            if (!selectedQuestionIds.isEmpty()) {
                List<Question> selectedQuestions = questionRepository.findAllById(selectedQuestionIds);
                for (Question question : selectedQuestions) {
                    Integer sourceType = question.getSourceType();
                    if (sourceType != null && sourceType == 2) {
                        question.setSimulationExamId(savedExam.getId());
                    }
                }
                questionRepository.saveAll(selectedQuestions);
            }

            for (Question linkedQuestion : linkedQuestions) {
                Integer sourceType = linkedQuestion.getSourceType();
                if (sourceType != null && sourceType == 2 && !selectedQuestionIds.contains(linkedQuestion.getId())) {
                    linkedQuestion.setSimulationExamId(null);
                }
            }
            questionRepository.saveAll(linkedQuestions);

            int questionCount = questionRepository.findQuestionBySimulationExamId(savedExam.getId()).size();
            savedExam.setQuestionCount(questionCount);
            simulationExamRepository.save(savedExam);

            return Result.ok(savedExam);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存考试失败：" + e.getMessage());
        }
    }

    private String getString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text)) {
            return null;
        }
        return text;
    }

    private LocalDateTime parseDateTime(String dateTimeText) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            return LocalDateTime.parse(dateTimeText, formatter);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(dateTimeText);
        } catch (Exception ignored) {
        }
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTimeText);
        return offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Set<Long> parseQuestionIds(Object questionIdsObject) {
        Set<Long> questionIds = new HashSet<>();
        if (!(questionIdsObject instanceof List<?> rawList)) {
            return questionIds;
        }
        for (Object item : rawList) {
            if (item == null) {
                continue;
            }
            String text = item.toString().trim();
            if (text.isEmpty()) {
                continue;
            }
            questionIds.add(Long.valueOf(text));
        }
        return questionIds;
    }

    @Transactional
    public Result deleteExam(String examId) {
        try {
            Long examIdLong = Long.valueOf(examId);
            SimulationExam exam = simulationExamRepository.findById(examIdLong).orElse(null);
            if (exam == null) {
                return Result.error("考试不存在");
            }

            List<Question> linkedQuestions = questionRepository.findQuestionBySimulationExamId(examIdLong);
            if (!linkedQuestions.isEmpty()) {
                return Result.error("该考试下存在题目，请先删除或移除相关题目");
            }

            testSampleResultRepository.deleteByExamId(examIdLong);
            simulationExamResultRepository.deleteByExamId(examIdLong);
            violationRecordRepository.deleteByExamId(examIdLong);
            simulationExamRepository.deleteById(examIdLong);

            return Result.ok().setMessage("删除考试成功");
        } catch (NumberFormatException e) {
            return Result.error("考试ID格式错误");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除考试失败：" + e.getMessage());
        }
    }

    public Result saveProgress(Map<String, Object> request) {
        try {
            String userId = getString(request.get("userId"));
            String examId = getString(request.get("examId"));
            if (userId == null || examId == null) {
                return Result.badRequest("保存进度需要userId和examId");
            }

            String key = buildProgressKey(userId, examId);
            String payload = objectMapper.writeValueAsString(request);
            stringRedisTemplate.opsForValue().set(key, payload, Duration.ofDays(7));

            return Result.ok().setMessage("保存进度成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存进度失败：" + e.getMessage());
        }
    }

    public Result getProgress(String userId, String examId) {
        try {
            String key = buildProgressKey(userId, examId);
            String payload = stringRedisTemplate.opsForValue().get(key);
            if (payload == null || payload.isEmpty()) {
                return Result.ok(new java.util.HashMap<String, Object>()).setMessage("暂无保存进度");
            }

            Map<String, Object> progress = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
            return Result.ok(progress);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("读取进度失败：" + e.getMessage());
        }
    }

    private String buildProgressKey(String userId, String examId) {
        return "exam_progress:" + userId + ":" + examId;
    }
}
