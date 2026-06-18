package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.entity.VipKey;
import sen.yuhuang.backend.repository.UserRepository;
import sen.yuhuang.backend.repository.VipKeyRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VipKeyService {

    private static final String KEY_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private VipKeyRepository vipKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Result generateKeys(Map<String, Object> request) {
        try {
            int count = parseInt(request.get("count"), 1);
            int durationDays = parseInt(request.get("durationDays"), 30);
            Long creatorId = parseLong(request.get("userId"));
            String note = toText(request.get("note"));

            if (creatorId == null) {
                return Result.badRequest("用户ID不能为空");
            }
            if (count < 1 || count > 50) {
                return Result.badRequest("生成数量需在1-50之间");
            }
            if (durationDays <= 0) {
                return Result.badRequest("开通天数必须大于0");
            }

            List<VipKey> keys = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                VipKey key = new VipKey();
                key.setKeyCode(generateKeyCode());
                key.setDurationDays(durationDays);
                key.setStatus(0);
                key.setCreatedBy(creatorId);
                key.setNote(note);
                keys.add(key);
            }

            List<VipKey> saved = vipKeyRepository.saveAll(keys);
            return Result.ok("成功生成 " + saved.size() + " 个密钥", saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成密钥失败：" + e.getMessage());
        }
    }

    public Result listKeys(Map<String, Object> request) {
        try {
            Long creatorId = parseLong(request.get("userId"));
            if (creatorId == null) {
                return Result.badRequest("用户ID不能为空");
            }

            List<VipKey> keys = vipKeyRepository.findByCreatedByOrderByCreateTimeDesc(creatorId);
            long unusedCount = vipKeyRepository.countByCreatedByAndStatus(creatorId, 0);
            long usedCount = vipKeyRepository.countByCreatedByAndStatus(creatorId, 1);

            var result = new java.util.HashMap<String, Object>();
            result.put("keys", buildKeyViews(keys));
            result.put("unusedCount", unusedCount);
            result.put("usedCount", usedCount);
            return Result.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询密钥失败：" + e.getMessage());
        }
    }

    @Transactional
    public Result redeemKey(Map<String, Object> request) {
        try {
            String keyCode = toText(request.get("keyCode"));
            Long userId = parseLong(request.get("userId"));

            if (keyCode == null || keyCode.isEmpty()) {
                return Result.badRequest("请输入密钥");
            }
            if (userId == null) {
                return Result.badRequest("用户ID不能为空");
            }

            Optional<VipKey> optionalKey = vipKeyRepository.findByKeyCode(keyCode.toUpperCase());
            if (optionalKey.isEmpty()) {
                return Result.badRequest("密钥不存在，请检查输入");
            }

            VipKey vipKey = optionalKey.get();
            if (vipKey.getStatus() == 1) {
                return Result.badRequest("该密钥已被使用");
            }

            User user = userRepository.findUserByUserId(userId);
            if (user == null) {
                return Result.badRequest("用户不存在");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime baseTime = (user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(now))
                    ? user.getVipExpireTime()
                    : now;
            LocalDateTime newExpireTime = baseTime.plusDays(vipKey.getDurationDays());

            user.setVipExpireTime(newExpireTime);
            if (user.getRoleId() == 1) {
                user.setRoleId(2L);
            }
            userRepository.save(user);

            vipKey.setStatus(1);
            vipKey.setUsedBy(userId);
            vipKey.setUsedTime(now);
            vipKeyRepository.save(vipKey);

            return Result.ok("VIP开通成功，到期时间：" + newExpireTime.toLocalDate(), user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("兑换失败：" + e.getMessage());
        }
    }

    @Transactional
    public Result deleteKey(Map<String, Object> request) {
        try {
            Long keyId = parseLong(request.get("id"));
            if (keyId == null) {
                return Result.badRequest("密钥ID不能为空");
            }

            Optional<VipKey> optionalKey = vipKeyRepository.findById(keyId);
            if (optionalKey.isEmpty()) {
                return Result.badRequest("密钥不存在");
            }

            VipKey vipKey = optionalKey.get();
            if (vipKey.getStatus() == 1) {
                return Result.badRequest("已使用的密钥不能删除");
            }

            vipKeyRepository.deleteById(keyId);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    private String generateKeyCode() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            if (i > 0 && i % 4 == 0) sb.append('-');
            sb.append(KEY_CHARS.charAt(RANDOM.nextInt(KEY_CHARS.length())));
        }
        return sb.toString();
    }

    private List<Map<String, Object>> buildKeyViews(List<VipKey> keys) {
        Set<Long> usedUserIds = keys.stream()
                .map(VipKey::getUsedBy)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, User> usedUsers = userRepository.findAllById(usedUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Map<String, Object>> keyViews = new ArrayList<>();
        for (VipKey key : keys) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", key.getId());
            row.put("keyCode", key.getKeyCode());
            row.put("durationDays", key.getDurationDays());
            row.put("status", key.getStatus());
            row.put("usedBy", key.getUsedBy());
            row.put("usedByDisplay", buildUsedByDisplay(usedUsers.get(key.getUsedBy()), key.getUsedBy()));
            row.put("usedTime", key.getUsedTime());
            row.put("createdBy", key.getCreatedBy());
            row.put("createTime", key.getCreateTime());
            row.put("note", key.getNote());
            keyViews.add(row);
        }
        return keyViews;
    }

    private String buildUsedByDisplay(User user, Long usedBy) {
        if (usedBy == null) return null;
        if (user == null) return "未知用户（ID:" + usedBy + "）";

        String realName = toText(user.getRealName());
        String username = toText(user.getUsername());
        String displayRealName = realName == null ? "未填写真名" : realName;
        String displayUsername = username == null ? "未知网名" : username;
        return displayRealName + "（" + displayUsername + "，ID:" + user.getId() + "）";
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        String text = value.toString().trim();
        if (text.isEmpty() || "null".equals(text) || "undefined".equals(text)) return null;
        return Long.valueOf(text);
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Integer) return (Integer) value;
        String text = value.toString().trim();
        if (text.isEmpty()) return defaultValue;
        return Integer.parseInt(text);
    }

    private String toText(Object value) {
        if (value == null) return null;
        String text = value.toString().trim();
        if (text.isEmpty() || "null".equalsIgnoreCase(text) || "undefined".equalsIgnoreCase(text)) return null;
        return text;
    }
}
