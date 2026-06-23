package sen.yuhuang.backend.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.InterviewCollege;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.repository.InterviewCollegeRepository;
import sen.yuhuang.backend.repository.RoleRepository;
import sen.yuhuang.backend.repository.UserLearningStatsRepository;
import sen.yuhuang.backend.repository.UserRepository;
import sen.yuhuang.backend.service.InviteCodeService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private InviteCodeService inviteCodeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLearningStatsRepository userLearningStatsRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private InterviewCollegeRepository interviewCollegeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 辅助方法 ====================

    /**
     * 从 X-Username header 获取当前管理员用户
     * 仅 roleId == 4 的用户允许访问
     */
    private User getAdminUser(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.isEmpty()) {
            return null;
        }
        User user = userRepository.findUserByUsername(username);
        if (user == null || user.getRoleId() == null || user.getRoleId() != 4) {
            return null;
        }
        return user;
    }

    /**
     * 将 User 实体转为 Map（排除 password 字段）
     */
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("phone", user.getPhone());
        map.put("college", user.getCollege());
        map.put("roleId", user.getRoleId());
        map.put("status", user.getStatus());
        map.put("email", user.getEmail());
        map.put("createTime", user.getCreateTime());
        return map;
    }

    // ==================== 邀请码管理 ====================

    /**
     * 生成邀请码
     */
    @PostMapping("/invite-codes")
    public Result generateInviteCodes(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        String college = (String) body.get("college");
        int count = 1;
        if (body.get("count") != null) {
            count = ((Number) body.get("count")).intValue();
        }
        String expiresAtStr = (String) body.get("expiresAt");

        if (expiresAtStr == null || expiresAtStr.isEmpty()) {
            return Result.badRequest("过期时间不能为空");
        }

        LocalDateTime expiresAt;
        try {
            // 支持 ISO 格式 (2026-06-22T14:30:00) 和空格格式 (2026-06-22 14:30:00)
            if (expiresAtStr.contains("T")) {
                expiresAt = LocalDateTime.parse(expiresAtStr);
            } else {
                expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        } catch (DateTimeParseException e) {
            return Result.badRequest("过期时间格式错误，请使用 ISO LocalDateTime 格式");
        }

        return inviteCodeService.generateCodes(college, count, expiresAt, admin.getId());
    }

    /**
     * 查询邀请码列表
     */
    @GetMapping("/invite-codes")
    public Result listInviteCodes(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String college,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        return inviteCodeService.listCodes(status, college, page, size);
    }

    /**
     * 作废邀请码
     */
    @DeleteMapping("/invite-codes/{id}")
    public Result revokeInviteCode(HttpServletRequest request, @PathVariable Long id) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        return inviteCodeService.revokeCode(id);
    }

    // ==================== 用户管理 ====================

    /**
     * 查询用户列表（支持按学院、角色、关键字筛选，分页）
     * 支持 roleIds 参数（逗号分隔），默认只查教师和管理员（roleId=3,4）
     */
    @SuppressWarnings("unchecked")
    @GetMapping("/users")
    public Result listUsers(
            HttpServletRequest request,
            @RequestParam(required = false) String college,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String roleIds,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        // 构建动态 JPQL
        StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");
        StringBuilder countJpql = new StringBuilder("SELECT COUNT(u) FROM User u WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (college != null && !college.isEmpty()) {
            jpql.append(" AND u.college = :college");
            countJpql.append(" AND u.college = :college");
            params.put("college", college);
        }
        // roleIds 优先于 roleId
        if (roleIds != null && !roleIds.isEmpty()) {
            String[] idStrs = roleIds.split(",");
            List<Long> roleIdList = new ArrayList<>();
            for (String idStr : idStrs) {
                try {
                    roleIdList.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException ignored) {}
            }
            if (!roleIdList.isEmpty()) {
                jpql.append(" AND u.roleId IN :roleIdList");
                countJpql.append(" AND u.roleId IN :roleIdList");
                params.put("roleIdList", roleIdList);
            }
        } else if (roleId != null) {
            jpql.append(" AND u.roleId = :roleId");
            countJpql.append(" AND u.roleId = :roleId");
            params.put("roleId", roleId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            jpql.append(" AND (u.username LIKE :keyword OR u.realName LIKE :keyword OR u.phone LIKE :keyword)");
            countJpql.append(" AND (u.username LIKE :keyword OR u.realName LIKE :keyword OR u.phone LIKE :keyword)");
            params.put("keyword", "%" + keyword + "%");
        }

        jpql.append(" ORDER BY u.createTime DESC");

        // 查询总数
        Query countQuery = entityManager.createQuery(countJpql.toString(), Long.class);
        params.forEach(countQuery::setParameter);
        long totalElements = (Long) countQuery.getSingleResult();

        // 分页查询
        Query dataQuery = entityManager.createQuery(jpql.toString(), User.class);
        params.forEach(dataQuery::setParameter);
        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);
        List<User> users = dataQuery.getResultList();

        // 转为 Map 列表（排除 password）
        List<Map<String, Object>> userMaps = new ArrayList<>();
        for (User user : users) {
            userMaps.add(userToMap(user));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", userMaps);
        data.put("totalElements", totalElements);
        data.put("totalPages", (int) Math.ceil((double) totalElements / size));
        data.put("number", page);
        data.put("size", size);

        return Result.ok(data);
    }

    /**
     * 修改用户角色
     */
    @Transactional
    @PutMapping("/users/{id}/role")
    public Result updateUserRole(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        if (admin.getId().equals(id)) {
            return Result.badRequest("不能修改自己的角色");
        }

        if (body.get("roleId") == null) {
            return Result.badRequest("roleId 不能为空");
        }

        Long roleId = ((Number) body.get("roleId")).longValue();
        User user = userRepository.findUserByUserId(id);
        if (user == null) {
            return Result.badRequest("用户不存在");
        }

        user.setRoleId(roleId);
        userRepository.save(user);

        return Result.ok("角色修改成功");
    }

    /**
     * 修改用户状态
     */
    @Transactional
    @PutMapping("/users/{id}/status")
    public Result updateUserStatus(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        if (body.get("status") == null) {
            return Result.badRequest("status 不能为空");
        }

        Integer status = ((Number) body.get("status")).intValue();
        User user = userRepository.findUserByUserId(id);
        if (user == null) {
            return Result.badRequest("用户不存在");
        }

        user.setStatus(status);
        userRepository.save(user);

        return Result.ok("状态修改成功");
    }

    /**
     * 修改用户学院
     */
    @Transactional
    @PutMapping("/users/{id}/college")
    public Result updateUserCollege(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        String college = (String) body.get("college");
        if (college == null || college.isEmpty()) {
            return Result.badRequest("学院不能为空");
        }

        User user = userRepository.findUserByUserId(id);
        if (user == null) {
            return Result.badRequest("用户不存在");
        }

        user.setCollege(college);
        userRepository.save(user);

        return Result.ok("学院修改成功");
    }

    // ==================== 统计概览 ====================

    /**
     * 管理后台概览统计：总用户数、教师数、学生数、考试次数
     * Redis 缓存，TTL 10 分钟
     */
    @GetMapping("/stats/overview")
    public Result getStatsOverview(HttpServletRequest request) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        String cacheKey = "stats:admin:overview";
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                Map<String, Object> cachedData = objectMapper.readValue(cached, new TypeReference<Map<String, Object>>() {});
                return Result.ok(cachedData);
            }
        } catch (Exception e) {
            // 缓存读取失败，继续查库
        }

        long totalUsers = userRepository.count();
        // 教师: roleId == 3 or roleId == 4，学生: 其余用户
        long teacherCount = countByRoleId(3L) + countByRoleId(4L);
        long studentCount = totalUsers - teacherCount;
        long totalExams = userLearningStatsRepository.count();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("totalTeachers", teacherCount);
        data.put("totalStudents", studentCount);
        data.put("totalExams", totalExams);

        // 写入缓存
        try {
            String json = objectMapper.writeValueAsString(data);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(10));
        } catch (Exception e) {
            // 缓存写入失败不影响返回
        }

        return Result.ok(data);
    }

    /**
     * 按学院统计：学院名、学生数、平均做题数、平均通过率
     * Redis 缓存，TTL 10 分钟
     */
    @GetMapping("/stats/colleges")
    public Result getCollegeStats(HttpServletRequest request) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        String cacheKey = "stats:admin:colleges";
        try {
            String cached = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                List<Map<String, Object>> cachedData = objectMapper.readValue(cached, new TypeReference<List<Map<String, Object>>>() {});
                return Result.ok(cachedData);
            }
        } catch (Exception e) {
            // 缓存读取失败，继续查库
        }

        // 查询各学院学员数（roleId IN 1,2）
        String studentSql = "SELECT u.college, COUNT(u) FROM User u WHERE u.college IS NOT NULL AND u.college <> '' AND u.roleId IN (1,2) GROUP BY u.college";
        Query studentQuery = entityManager.createQuery(studentSql);
        List<Object[]> studentResults = studentQuery.getResultList();

        // 查询各学院教师数（roleId = 3 or roleId = 4，与概览统计一致）
        String teacherSql = "SELECT u.college, COUNT(u) FROM User u WHERE u.college IS NOT NULL AND u.college <> '' AND u.roleId IN (3,4) GROUP BY u.college";
        Query teacherQuery = entityManager.createQuery(teacherSql);
        List<Object[]> teacherResults = teacherQuery.getResultList();
        Map<String, Long> teacherMap = new HashMap<>();
        for (Object[] row : teacherResults) {
            teacherMap.put((String) row[0], (Long) row[1]);
        }

        // 查询各学院平均做题数和平均通过率（仅学员 roleId IN 1,2）
        String statsSql = "SELECT u.college, AVG(s.questionCount), AVG(s.questionPassRate) " +
                "FROM User u JOIN UserLearningStats s ON u.id = s.userId " +
                "WHERE u.college IS NOT NULL AND u.college <> '' AND u.roleId IN (1,2) " +
                "GROUP BY u.college";
        Query statsQuery = entityManager.createQuery(statsSql);
        List<Object[]> statsResults = statsQuery.getResultList();

        // 整理 stats 数据为 Map
        Map<String, Object[]> statsMap = new HashMap<>();
        for (Object[] row : statsResults) {
            String college = (String) row[0];
            statsMap.put(college, row);
        }

        // 合并结果
        List<Map<String, Object>> collegeStats = new ArrayList<>();
        Set<String> processedColleges = new HashSet<>();
        for (Object[] row : studentResults) {
            String college = (String) row[0];
            long studentCount = (Long) row[1];
            long teacherCount = teacherMap.getOrDefault(college, 0L);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("college", college);
            item.put("studentCount", studentCount);
            item.put("teacherCount", teacherCount);

            Object[] stats = statsMap.get(college);
            if (stats != null) {
                item.put("avgQuestionCount", stats[1] != null ? Math.round(((Number) stats[1]).doubleValue() * 100.0) / 100.0 : 0);
                item.put("avgPassRate", stats[2] != null ? Math.round(((Number) stats[2]).doubleValue() * 100.0) / 100.0 : 0);
            } else {
                item.put("avgQuestionCount", 0);
                item.put("avgPassRate", 0);
            }

            collegeStats.add(item);
            processedColleges.add(college);
        }

        // 补充有教师但无学员的学院
        for (Map.Entry<String, Long> entry : teacherMap.entrySet()) {
            String college = entry.getKey();
            if (!processedColleges.contains(college)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("college", college);
                item.put("studentCount", 0L);
                item.put("teacherCount", entry.getValue());
                item.put("avgQuestionCount", 0);
                item.put("avgPassRate", 0);
                collegeStats.add(item);
            }
        }

        // 写入缓存
        try {
            String json = objectMapper.writeValueAsString(collegeStats);
            stringRedisTemplate.opsForValue().set(cacheKey, json, Duration.ofMinutes(10));
        } catch (Exception e) {
            // 缓存写入失败不影响返回
        }

        return Result.ok(collegeStats);
    }

    // ==================== 学院管理 ====================

    /**
     * 查询学院列表（管理员：全部；普通用户：仅启用）
     */
    @GetMapping("/colleges")
    public Result listColleges(HttpServletRequest request,
                               @RequestParam(required = false) Integer enabled) {
        // 允许非管理员访问（用于下拉选择），但只返回启用的
        String username = request.getHeader("X-Username");
        boolean isAdmin = false;
        if (username != null && !username.isEmpty()) {
            User user = userRepository.findUserByUsername(username);
            isAdmin = user != null && user.getRoleId() != null && user.getRoleId() == 4;
        }

        List<InterviewCollege> colleges;
        if (isAdmin) {
            // 管理员看全部（可按 enabled 筛选）
            if (enabled != null) {
                colleges = interviewCollegeRepository.findByEnabledOrderBySortOrderAsc(enabled);
            } else {
                colleges = interviewCollegeRepository.findAllByOrderBySortOrderAsc();
            }
        } else {
            // 非管理员只看启用的
            colleges = interviewCollegeRepository.findByEnabledOrderBySortOrderAsc(1);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (InterviewCollege c : colleges) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("sortOrder", c.getSortOrder());
            map.put("enabled", c.getEnabled());
            map.put("createTime", c.getCreateTime());
            map.put("updateTime", c.getUpdateTime());
            list.add(map);
        }
        return Result.ok(list);
    }

    /**
     * 新增学院
     */
    @PostMapping("/colleges")
    public Result createCollege(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        String name = (String) body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("学院名称不能为空");
        }

        InterviewCollege existing = interviewCollegeRepository.findByName(name.trim());
        if (existing != null) {
            return Result.badRequest("学院名称已存在");
        }

        InterviewCollege college = new InterviewCollege();
        college.setName(name.trim());
        college.setSortOrder(body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0);
        college.setEnabled(body.get("enabled") != null ? ((Number) body.get("enabled")).intValue() : 1);
        interviewCollegeRepository.save(college);

        return Result.ok("学院创建成功");
    }

    /**
     * 修改学院
     */
    @PutMapping("/colleges/{id}")
    public Result updateCollege(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        InterviewCollege college = interviewCollegeRepository.findById(id).orElse(null);
        if (college == null) {
            return Result.badRequest("学院不存在");
        }

        if (body.containsKey("name")) {
            String name = (String) body.get("name");
            if (name == null || name.trim().isEmpty()) {
                return Result.badRequest("学院名称不能为空");
            }
            InterviewCollege existing = interviewCollegeRepository.findByName(name.trim());
            if (existing != null && !existing.getId().equals(id)) {
                return Result.badRequest("学院名称已存在");
            }
            college.setName(name.trim());
        }
        if (body.containsKey("sortOrder")) {
            college.setSortOrder(((Number) body.get("sortOrder")).intValue());
        }
        if (body.containsKey("enabled")) {
            college.setEnabled(((Number) body.get("enabled")).intValue());
        }
        interviewCollegeRepository.save(college);

        return Result.ok("学院更新成功");
    }

    /**
     * 删除学院
     */
    @DeleteMapping("/colleges/{id}")
    public Result deleteCollege(HttpServletRequest request, @PathVariable Long id) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        InterviewCollege college = interviewCollegeRepository.findById(id).orElse(null);
        if (college == null) {
            return Result.badRequest("学院不存在");
        }

        interviewCollegeRepository.deleteById(id);
        return Result.ok("学院删除成功");
    }

    /**
     * 初始化学院数据（将13个学院插入数据库）
     */
    @PostMapping("/colleges/init")
    public Result initColleges(HttpServletRequest request) {
        User admin = getAdminUser(request);
        if (admin == null) {
            return Result.forbidden("无管理员权限");
        }

        List<String> collegeNames = Arrays.asList(
            "信息与通信工程学院", "计算机学院", "自动化学院",
            "机械工程学院", "经济管理学院", "信息管理学院",
            "马克思主义学院", "外国语学院", "理学院",
            "公共管理与传媒学院", "国际交流学院",
            "电子信息学院", "人文社科学院"
        );

        int inserted = 0;
        int existed = 0;

        for (String name : collegeNames) {
            InterviewCollege existing = interviewCollegeRepository.findByName(name);
            if (existing == null) {
                InterviewCollege college = new InterviewCollege();
                college.setName(name);
                college.setSortOrder(inserted + 1);
                college.setEnabled(1);
                interviewCollegeRepository.save(college);
                inserted++;
            } else {
                existed++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inserted", inserted);
        result.put("existed", existed);
        result.put("total", collegeNames.size());
        return Result.ok(result);
    }

    // ==================== 内部辅助查询 ====================

    /**
     * 按 roleId 统计用户数
     */
    private long countByRoleId(Long roleId) {
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.roleId = :roleId";
        Query query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("roleId", roleId);
        return (Long) query.getSingleResult();
    }
}
