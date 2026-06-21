package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.SimulationExam;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.entity.ViolationRecord;
import sen.yuhuang.backend.repository.SimulationExamRepository;
import sen.yuhuang.backend.repository.UserRepository;
import sen.yuhuang.backend.repository.ViolationRecordRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ViolationRecordService {
    private static final Pattern VIOLATION_PATTERN = Pattern.compile("违规类型:(.*?) 次数:(\\d+)");

    @Autowired
    SimulationExamRepository simulationExamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ViolationRecordRepository violationRecordRepository;

    public Result addViolationRecord(String examId, String userId, String violationType, String count) {
        try {
            //确认examId是否存在
            SimulationExam exam = simulationExamRepository.getExamByExamId(Long.valueOf(examId));
            if (exam == null) return Result.badRequest("该场考试不存在！");

            User user = userRepository.findUserByUserId(Long.valueOf(userId));
            if (user == null) return Result.badRequest("违规用户不存在！");

            ViolationRecord violationRecord = ViolationRecord.builder()
                    .examId(Long.valueOf(examId))
                    .userId(Long.valueOf(userId))
                    .violationDesc("违规类型:"+violationType+" 次数:"+count)
                    .build();
            violationRecordRepository.save(violationRecord);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.badRequest(e.getMessage());
        }

    }

    public Result listViolationRecords() {
        try {
            List<ViolationRecord> records = violationRecordRepository.findAllByOrderByCreateTimeDesc();
            Set<Long> userIds = records.stream()
                    .map(ViolationRecord::getUserId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            Set<Long> examIds = records.stream()
                    .map(ViolationRecord::getExamId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());

            Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
            Map<Long, SimulationExam> examMap = simulationExamRepository.findAllById(examIds).stream()
                    .collect(Collectors.toMap(SimulationExam::getId, Function.identity()));

            List<Map<String, Object>> rows = new ArrayList<>();
            for (ViolationRecord record : records) {
                User user = userMap.get(record.getUserId());
                SimulationExam exam = examMap.get(record.getExamId());
                ViolationInfo violationInfo = parseViolationInfo(record.getViolationDesc());

                Map<String, Object> row = new HashMap<>();
                row.put("id", record.getId());
                row.put("examId", record.getExamId());
                row.put("examTitle", exam == null ? "未知考试" : exam.getExamTitle());
                row.put("userId", record.getUserId());
                row.put("username", user == null ? null : user.getUsername());
                row.put("realName", user == null ? null : user.getRealName());
                row.put("studentDisplay", buildStudentDisplay(user, record.getUserId()));
                row.put("violationDesc", record.getViolationDesc());
                row.put("violationType", violationInfo.violationType());
                row.put("count", violationInfo.count());
                row.put("createTime", record.getCreateTime());
                rows.add(row);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("records", rows);
            result.put("total", rows.size());
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询违规记录失败：" + e.getMessage());
        }
    }

    private ViolationInfo parseViolationInfo(String violationDesc) {
        if (violationDesc == null) return new ViolationInfo("未知违规", 0);
        Matcher matcher = VIOLATION_PATTERN.matcher(violationDesc);
        if (!matcher.find()) return new ViolationInfo(violationDesc, 0);
        return new ViolationInfo(matcher.group(1), Integer.parseInt(matcher.group(2)));
    }

    private String buildStudentDisplay(User user, Long userId) {
        if (user == null) return "未知学生（ID:" + userId + "）";
        String realName = toText(user.getRealName());
        String username = toText(user.getUsername());
        String displayRealName = realName == null ? "未填写真名" : realName;
        String displayUsername = username == null ? "未知网名" : username;
        return displayRealName + "（" + displayUsername + "，ID:" + user.getId() + "）";
    }

    private String toText(String value) {
        if (value == null) return null;
        String text = value.trim();
        return text.isEmpty() ? null : text;
    }

    private record ViolationInfo(String violationType, Integer count) {}
}
