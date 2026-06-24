package sen.yuhuang.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import sen.yuhuang.backend.repository.SimulationExamResultRepository;
import sen.yuhuang.backend.repository.UserLearningStatsRepository;
import sen.yuhuang.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeacherStatsService {

    private final UserRepository userRepository;
    private final UserLearningStatsRepository userLearningStatsRepository;
    private final SimulationExamResultRepository simulationExamResultRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PersistenceContext
    private EntityManager entityManager;

    // ==================== 学院概览统计 ====================

    /**
     * 获取概览统计（college为null时统计全校）
     */
    public Map<String, Object> getOverview(String college) {
        String cacheKey = "stats:college:" + (college != null ? college : "all");
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {}
        }

        Map<String, Object> result = new HashMap<>();
        String collegeCondition = college != null ? " AND u.college = :college" : "";

        // 学生数量
        String countSql = "SELECT COUNT(*) FROM user u WHERE u.role_id IN (1,2)" + collegeCondition;
        Query countQuery = entityManager.createNativeQuery(countSql);
        if (college != null) countQuery.setParameter("college", college);
        long studentCount = ((Number) countQuery.getSingleResult()).longValue();
        result.put("studentCount", studentCount);

        // 平均做题数量 & 平均通过率
        String statsSql = "SELECT AVG(s.question_count), AVG(s.question_pass_rate) " +
                "FROM user_learning_stats s INNER JOIN user u ON s.user_id = u.id " +
                "WHERE u.role_id IN (1,2)" + collegeCondition;
        Query statsQuery = entityManager.createNativeQuery(statsSql);
        if (college != null) statsQuery.setParameter("college", college);
        Object[] statsRow = (Object[]) statsQuery.getSingleResult();
        result.put("avgQuestionCount", statsRow[0] != null ? ((BigDecimal) statsRow[0]).doubleValue() : 0.0);
        result.put("avgPassRate", statsRow[1] != null ? ((BigDecimal) statsRow[1]).doubleValue() : 0.0);

        // 考试通过率
        String examCond = college != null ? " AND u.college = :college" : "";
        String examTotalSql = "SELECT COUNT(*) FROM simulation_exam_result ser " +
                "INNER JOIN user u ON ser.user_id = u.id WHERE u.role_id IN (1,2)" + examCond;
        Query examTotalQuery = entityManager.createNativeQuery(examTotalSql);
        if (college != null) examTotalQuery.setParameter("college", college);
        long examTotal = ((Number) examTotalQuery.getSingleResult()).longValue();

        String examPassedSql = "SELECT COUNT(*) FROM simulation_exam_result ser " +
                "INNER JOIN user u ON ser.user_id = u.id WHERE u.role_id IN (1,2) AND ser.is_passed = 1" + examCond;
        Query examPassedQuery = entityManager.createNativeQuery(examPassedSql);
        if (college != null) examPassedQuery.setParameter("college", college);
        long examPassed = ((Number) examPassedQuery.getSingleResult()).longValue();

        double examPassRate = examTotal > 0 ? (double) examPassed / examTotal * 100 : 0.0;
        result.put("examPassRate", Math.round(examPassRate * 100.0) / 100.0);

        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(10));
        } catch (Exception e) {}

        return result;
    }

    // ==================== 学生列表（分页） ====================

    /**
     * 获取学生列表（college为null时查全校）
     */
    public Map<String, Object> getStudents(String college, String keyword, int page, int size) {
        String cacheKey = "stats:college:" + (college != null ? college : "all") + ":students:page:" + page + ":" + (keyword != null ? keyword : "");
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {}
        }

        Map<String, Object> result = new HashMap<>();
        String collegeCondition = college != null ? " AND u.college = :college" : "";

        String baseCondition = "u.role_id IN (1,2)" + collegeCondition;
        if (keyword != null && !keyword.trim().isEmpty()) {
            baseCondition += " AND (u.username LIKE :keyword OR u.real_name LIKE :keyword)";
        }

        // 查询总数
        String countSql = "SELECT COUNT(*) FROM user u WHERE " + baseCondition;
        Query countQuery = entityManager.createNativeQuery(countSql);
        if (college != null) countQuery.setParameter("college", college);
        if (keyword != null && !keyword.trim().isEmpty()) {
            countQuery.setParameter("keyword", "%" + keyword.trim() + "%");
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // 查询学生列表 + 学习统计
        String dataSql = "SELECT u.id, u.username, u.real_name, " +
                "s.question_count, s.question_pass_rate, s.exam_count, u.create_time " +
                "FROM user u LEFT JOIN user_learning_stats s ON u.id = s.user_id " +
                "WHERE " + baseCondition + " ORDER BY u.create_time DESC LIMIT :size OFFSET :offset";
        Query dataQuery = entityManager.createNativeQuery(dataSql)
                .setParameter("size", size)
                .setParameter("offset", page * size);
        if (college != null) dataQuery.setParameter("college", college);
        if (keyword != null && !keyword.trim().isEmpty()) {
            dataQuery.setParameter("keyword", "%" + keyword.trim() + "%");
        }

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();

        List<Map<String, Object>> students = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> student = new HashMap<>();
            Long studentId = ((Number) row[0]).longValue();
            student.put("id", studentId);
            student.put("username", row[1]);
            student.put("realName", row[2]);
            student.put("questionCount", row[3] != null ? ((Number) row[3]).intValue() : 0);
            student.put("questionPassRate", row[4] != null ? ((BigDecimal) row[4]).doubleValue() : 0.0);
            student.put("examCount", row[5] != null ? ((Number) row[5]).intValue() : 0);

            String avgScoreSql = "SELECT AVG(exam_total_score) FROM simulation_exam_result WHERE user_id = :userId";
            Object avgScore = entityManager.createNativeQuery(avgScoreSql)
                    .setParameter("userId", studentId)
                    .getSingleResult();
            student.put("avgScore", avgScore != null ? ((BigDecimal) avgScore).doubleValue() : 0.0);

            String lastActiveSql = "SELECT MAX(create_time) FROM simulation_exam_result WHERE user_id = :userId";
            Object lastActiveTime = entityManager.createNativeQuery(lastActiveSql)
                    .setParameter("userId", studentId)
                    .getSingleResult();
            student.put("lastActiveTime", lastActiveTime);

            students.add(student);
        }

        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("students", students);

        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(5));
        } catch (Exception e) {}

        return result;
    }

    // ==================== 学生详情 ====================

    /**
     * 获取学生详情（college为null时不按学院过滤）
     */
    public Map<String, Object> getStudentDetail(String college, Long studentId) {
        String collegeCondition = college != null ? " AND college = :college" : "";
        String userSql = "SELECT id, username, real_name, college, role_id, create_time " +
                "FROM user WHERE id = :studentId AND role_id IN (1,2)" + collegeCondition;
        Query userQuery = entityManager.createNativeQuery(userSql).setParameter("studentId", studentId);
        if (college != null) userQuery.setParameter("college", college);

        @SuppressWarnings("unchecked")
        List<Object[]> userRows = userQuery.getResultList();

        if (userRows.isEmpty()) {
            return null;
        }

        Object[] userRow = userRows.get(0);
        Map<String, Object> result = new HashMap<>();

        // 用户基本信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", ((Number) userRow[0]).longValue());
        userInfo.put("username", userRow[1]);
        userInfo.put("realName", userRow[2]);
        userInfo.put("college", userRow[3]);
        userInfo.put("roleId", userRow[4] != null ? ((Number) userRow[4]).longValue() : null);
        userInfo.put("createTime", userRow[5]);
        result.put("user", userInfo);

        // 学习统计
        String statsSql = "SELECT question_count, success_count, question_pass_rate, exam_count, note_count " +
                "FROM user_learning_stats WHERE user_id = :userId";
        @SuppressWarnings("unchecked")
        List<Object[]> statsRows = entityManager.createNativeQuery(statsSql)
                .setParameter("userId", studentId)
                .getResultList();

        if (!statsRows.isEmpty()) {
            Object[] statsRow = statsRows.get(0);
            Map<String, Object> stats = new HashMap<>();
            stats.put("questionCount", statsRow[0] != null ? ((Number) statsRow[0]).intValue() : 0);
            stats.put("successCount", statsRow[1] != null ? ((Number) statsRow[1]).intValue() : 0);
            stats.put("questionPassRate", statsRow[2] != null ? ((BigDecimal) statsRow[2]).doubleValue() : 0.0);
            stats.put("examCount", statsRow[3] != null ? ((Number) statsRow[3]).intValue() : 0);
            stats.put("noteCount", statsRow[4] != null ? ((Number) statsRow[4]).intValue() : 0);
            result.put("learningStats", stats);
        } else {
            result.put("learningStats", new HashMap<>());
        }

        // 最近考试记录（最近10条）
        String examSql = "SELECT exam_id, exam_total_score, is_passed, create_time " +
                "FROM simulation_exam_result WHERE user_id = :userId " +
                "ORDER BY create_time DESC LIMIT 10";
        @SuppressWarnings("unchecked")
        List<Object[]> examRows = entityManager.createNativeQuery(examSql)
                .setParameter("userId", studentId)
                .getResultList();

        List<Map<String, Object>> recentExams = new ArrayList<>();
        for (Object[] row : examRows) {
            Map<String, Object> exam = new HashMap<>();
            exam.put("examId", row[0] != null ? ((Number) row[0]).longValue() : null);
            exam.put("examTotalScore", row[1] != null ? ((BigDecimal) row[1]).doubleValue() : 0.0);
            exam.put("isPassed", row[2] != null && ((Number) row[2]).intValue() == 1);
            exam.put("createTime", row[3]);
            recentExams.add(exam);
        }
        result.put("recentExams", recentExams);

        return result;
    }
}
