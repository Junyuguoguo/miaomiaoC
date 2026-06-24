package sen.yuhuang.backend.controller.teacher;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.repository.UserRepository;
import sen.yuhuang.backend.service.TeacherStatsService;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherStatsController {

    @Autowired
    private TeacherStatsService teacherStatsService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 从请求头获取当前用户，校验教师权限（roleId >= 3）
     */
    private User getCurrentTeacher(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            return null;
        }
        // roleId >= 3 为教师/管理员
        if (user.getRoleId() == null || user.getRoleId() < 3) {
            return null;
        }
        return user;
    }

    /**
     * 获取学院概览统计（管理员看全校，教师看本学院）
     */
    @GetMapping("/stats/overview")
    public Result getOverview(HttpServletRequest request) {
        User teacher = getCurrentTeacher(request);
        if (teacher == null) {
            return Result.forbidden("无权限访问");
        }
        try {
            // 管理员不按学院过滤，看全校数据
            String college = teacher.getRoleId() == 4 ? null : teacher.getCollege();
            Map<String, Object> overview = teacherStatsService.getOverview(college);
            return Result.ok(overview);
        } catch (Exception e) {
            return Result.error("获取概览统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生列表（管理员看全校，教师看本学院）
     */
    @GetMapping("/stats/students")
    public Result getStudents(
            HttpServletRequest request,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User teacher = getCurrentTeacher(request);
        if (teacher == null) {
            return Result.forbidden("无权限访问");
        }
        try {
            String college = teacher.getRoleId() == 4 ? null : teacher.getCollege();
            Map<String, Object> students = teacherStatsService.getStudents(college, keyword, page, size);
            return Result.ok(students);
        } catch (Exception e) {
            return Result.error("获取学生列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生详情（管理员可看任意学生，教师只能看本学院）
     */
    @GetMapping("/stats/students/{id}")
    public Result getStudentDetail(HttpServletRequest request, @PathVariable("id") Long id) {
        User teacher = getCurrentTeacher(request);
        if (teacher == null) {
            return Result.forbidden("无权限访问");
        }
        try {
            String college = teacher.getRoleId() == 4 ? null : teacher.getCollege();
            Map<String, Object> detail = teacherStatsService.getStudentDetail(college, id);
            if (detail == null) {
                return Result.notFound("学生不存在");
            }
            return Result.ok(detail);
        } catch (Exception e) {
            return Result.error("获取学生详情失败：" + e.getMessage());
        }
    }
}
