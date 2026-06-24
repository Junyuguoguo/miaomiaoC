package sen.yuhuang.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.TeacherInviteCode;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.repository.TeacherInviteCodeRepository;
import sen.yuhuang.backend.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final TeacherInviteCodeRepository inviteCodeRepository;
    private final UserRepository userRepository;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成邀请码
     */
    @Transactional
    public Result generateCodes(String college, int count, LocalDateTime expiresAt, Long createdBy) {
        if (college == null || college.trim().isEmpty()) {
            return Result.badRequest("学院不能为空");
        }
        if (count < 1 || count > 50) {
            return Result.badRequest("数量范围 1-50");
        }
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            return Result.badRequest("过期时间必须大于当前时间");
        }

        for (int i = 0; i < count; i++) {
            TeacherInviteCode code = new TeacherInviteCode();
            code.setCode(generateCode());
            code.setCollege(college.trim());
            code.setCreatedBy(createdBy);
            code.setExpiresAt(expiresAt);
            code.setStatus(0);
            inviteCodeRepository.save(code);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        data.put("college", college.trim());
        return Result.ok("生成成功", data);
    }

    /**
     * 查询邀请码列表（返回用户名而非用户ID）
     */
    public Result listCodes(Integer status, String college, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TeacherInviteCode> result;

        if (status != null && college != null && !college.isEmpty()) {
            result = inviteCodeRepository.findByStatusAndCollegeOrderByCreateTimeDesc(status, college, pageRequest);
        } else if (status != null) {
            result = inviteCodeRepository.findByStatusOrderByCreateTimeDesc(status, pageRequest);
        } else if (college != null && !college.isEmpty()) {
            result = inviteCodeRepository.findByCollegeOrderByCreateTimeDesc(college, pageRequest);
        } else {
            result = inviteCodeRepository.findAllByOrderByCreateTimeDesc(pageRequest);
        }

        // 收集所有需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        for (TeacherInviteCode code : result.getContent()) {
            if (code.getCreatedBy() != null) userIds.add(code.getCreatedBy());
            if (code.getUsedBy() != null) userIds.add(code.getUsedBy());
        }

        // 批量查询用户名
        Map<Long, String> usernameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userRepository.findAllById(userIds);
            for (User user : users) {
                usernameMap.put(user.getId(), user.getUsername());
            }
        }

        // 构建返回数据
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> content = new ArrayList<>();
        for (TeacherInviteCode code : result.getContent()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", code.getId());
            item.put("code", code.getCode());
            item.put("college", code.getCollege());
            item.put("status", code.getStatus());
            item.put("createdBy", usernameMap.getOrDefault(code.getCreatedBy(), String.valueOf(code.getCreatedBy())));
            item.put("usedBy", code.getUsedBy() != null ? usernameMap.getOrDefault(code.getUsedBy(), String.valueOf(code.getUsedBy())) : null);
            item.put("createTime", code.getCreateTime() != null ? dtf.format(code.getCreateTime()) : null);
            item.put("expiresAt", code.getExpiresAt() != null ? dtf.format(code.getExpiresAt()) : null);
            content.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("totalPages", result.getTotalPages());
        data.put("totalElements", result.getTotalElements());
        data.put("number", result.getNumber());
        return Result.ok(data);
    }

    /**
     * 作废邀请码
     */
    @Transactional
    public Result revokeCode(Long id) {
        int updated = inviteCodeRepository.revokeById(id);
        if (updated == 0) {
            return Result.badRequest("邀请码不存在或已被使用");
        }
        return Result.ok("已作废");
    }

    /**
     * 校验并使用邀请码（注册时调用）
     */
    @Transactional
    public TeacherInviteCode validateAndUseCode(String code, Long userId) {
        TeacherInviteCode invite = inviteCodeRepository.findByCode(code);
        if (invite == null) return null;
        if (invite.getStatus() != 0) return null;
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) return null;

        inviteCodeRepository.updateUsage(invite.getId(), 1, userId, LocalDateTime.now());
        return invite;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
