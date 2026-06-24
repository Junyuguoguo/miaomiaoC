package sen.yuhuang.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.ChatMessageResponse;
import sen.yuhuang.backend.entity.ChatRoom;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.entity.UserRoomSetting;
import sen.yuhuang.backend.repository.UserRepository;
import sen.yuhuang.backend.service.ChatMessageService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    /**
     * resolve current user from X-Username header
     */
    private User getCurrentUser(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("not logged in");
        }
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("user not found");
        }
        return user;
    }

    @GetMapping("/private/{userId2}")
    public Result getPrivateMessages(
            HttpServletRequest request,
            @PathVariable Long userId2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = getCurrentUser(request);
        Page<ChatMessageResponse> messages = chatMessageService.getPrivateMessages(
                currentUser.getId(), userId2, page, size);
        return Result.ok(messages);
    }

    @GetMapping("/room/{roomId}")
    public Result getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ChatMessageResponse> messages = chatMessageService.getRoomMessages(roomId, page, size);
        return Result.ok(messages);
    }

    @GetMapping("/contacts")
    public Result getRecentContacts(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        List<Long> contacts = chatMessageService.getRecentContacts(currentUser.getId());
        return Result.ok(contacts);
    }

    @GetMapping("/unread-count")
    public Result getUnreadCount(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        long count = chatMessageService.getUnreadCount(currentUser.getId());
        return Result.ok(count);
    }

    @PostMapping("/read")
    public Result markAsRead(HttpServletRequest request, @RequestBody List<Long> messageIds) {
        User currentUser = getCurrentUser(request);
        chatMessageService.markAsRead(currentUser.getId(), messageIds);
        return Result.ok();
    }

    @PostMapping("/read-all/{contactId}")
    public Result markAllAsRead(HttpServletRequest request, @PathVariable Long contactId) {
        User currentUser = getCurrentUser(request);
        chatMessageService.markAllAsRead(currentUser.getId(), contactId);
        return Result.ok();
    }

    @GetMapping("/rooms")
    public Result getRooms(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        return Result.ok(chatMessageService.getVisibleRooms(currentUser.getId()));
    }

    @GetMapping("/contacts/detail")
    public Result getContactDetails(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        List<Map<String, Object>> contacts = chatMessageService.getContactDetails(currentUser.getId());
        return Result.ok(contacts);
    }

    @GetMapping("/users/search")
    public Result searchUsers(HttpServletRequest request, @RequestParam String keyword) {
        User currentUser = getCurrentUser(request);
        List<Map<String, Object>> users = chatMessageService.searchUsers(currentUser.getId(), keyword);
        return Result.ok(users);
    }

    @PostMapping("/rooms/{roomId}/join")
    public Result joinRoom(HttpServletRequest request, @PathVariable Long roomId) {
        User currentUser = getCurrentUser(request);
        chatMessageService.joinRoom(currentUser.getId(), roomId);
        return Result.ok();
    }

    @PostMapping("/rooms/{roomId}/leave")
    public Result leaveRoom(HttpServletRequest request, @PathVariable Long roomId) {
        User currentUser = getCurrentUser(request);
        chatMessageService.leaveRoom(currentUser.getId(), roomId);
        return Result.ok();
    }

    @GetMapping("/rooms/my")
    public Result getMyRooms(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        List<ChatRoom> rooms = chatMessageService.getUserRooms(currentUser.getId());
        return Result.ok(rooms);
    }

    @PostMapping("/rooms")
    public Result createRoom(HttpServletRequest request, @RequestBody Map<String, String> body) {
        User currentUser = getCurrentUser(request);
        if (currentUser.getRoleId() == null || currentUser.getRoleId() < 3) {
            return Result.badRequest("仅教师可创建聊天室");
        }
        String name = body.get("name");
        String college = body.get("college");
        String description = body.get("description");
        String roomLevel = body.get("roomLevel");
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("房间名不能为空");
        }
        return Result.ok(chatMessageService.createRoom(name, description, college, currentUser.getId(), roomLevel));
    }

    @DeleteMapping("/rooms/{roomId}")
    public Result deleteRoom(HttpServletRequest request, @PathVariable Long roomId) {
        User currentUser = getCurrentUser(request);
        if (currentUser.getRoleId() == null || currentUser.getRoleId() < 3) {
            return Result.badRequest("仅教师可删除聊天室");
        }
        chatMessageService.deleteRoom(roomId);
        return Result.ok("删除成功");
    }

    @PutMapping("/rooms/{roomId}")
    public Result updateRoom(HttpServletRequest request, @PathVariable Long roomId, @RequestBody Map<String, String> body) {
        User currentUser = getCurrentUser(request);
        if (currentUser.getRoleId() == null || currentUser.getRoleId() < 3) {
            return Result.badRequest("仅教师可修改聊天室");
        }
        ChatRoom room = chatMessageService.findById(roomId);
        if (room == null) return Result.badRequest("聊天室不存在");
        if (body.containsKey("roomName")) room.setRoomName(body.get("roomName"));
        if (body.containsKey("college")) room.setCollege(body.get("college"));
        if (body.containsKey("roomLevel")) room.setRoomLevel(body.get("roomLevel"));
        if (body.containsKey("description")) room.setDescription(body.get("description"));
        return Result.ok(chatMessageService.saveRoom(room));
    }

    /**
     * 根据群号加入房间
     */
    @GetMapping("/rooms/join/{groupNumber}")
    public Result joinByGroupNumber(HttpServletRequest request, @PathVariable String groupNumber) {
        User currentUser = getCurrentUser(request);
        ChatRoom room = chatMessageService.findByGroupNumber(groupNumber);
        if (room == null) {
            return Result.badRequest("群号不存在");
        }
        // 综合交流大厅不需要加入，所有人都能看
        if ("综合交流大厅".equals(room.getRoomName())) {
            return Result.ok(room);
        }
        // VIP房间需要VIP权限
        if ("VIP".equals(room.getRoomLevel())) {
            if (currentUser.getVipExpireTime() == null || currentUser.getVipExpireTime().isBefore(java.time.LocalDateTime.now())) {
                return Result.badRequest("该群为VIP专属群，请先开通VIP");
            }
        }
        // 加入房间
        chatMessageService.joinRoom(currentUser.getId(), room.getId());
        return Result.ok(room);
    }

    /**
     * 切换房间置顶状态
     */
    @PostMapping("/rooms/{roomId}/pin")
    public Result togglePin(HttpServletRequest request, @PathVariable Long roomId) {
        User currentUser = getCurrentUser(request);
        UserRoomSetting setting = chatMessageService.togglePin(currentUser.getId(), roomId);
        return Result.ok(setting);
    }

    /**
     * 切换房间免打扰状态
     */
    @PostMapping("/rooms/{roomId}/mute")
    public Result toggleMute(HttpServletRequest request, @PathVariable Long roomId) {
        User currentUser = getCurrentUser(request);
        UserRoomSetting setting = chatMessageService.toggleMute(currentUser.getId(), roomId);
        return Result.ok(setting);
    }

    /**
     * 获取用户的房间设置列表
     */
    @GetMapping("/rooms/settings")
    public Result getRoomSettings(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        List<UserRoomSetting> settings = chatMessageService.getUserRoomSettings(currentUser.getId());
        return Result.ok(settings);
    }

    /**
     * 获取房间详情
     */
    @GetMapping("/rooms/{roomId}/detail")
    public Result getRoomDetail(@PathVariable Long roomId) {
        ChatRoom room = chatMessageService.findById(roomId);
        if (room == null) return Result.badRequest("房间不存在");
        return Result.ok(room);
    }

    /**
     * 获取房间成员列表（含用户详情）
     */
    @GetMapping("/rooms/{roomId}/members")
    public Result getRoomMembers(@PathVariable Long roomId) {
        List<Map<String, Object>> members = chatMessageService.getRoomMembers(roomId);
        return Result.ok(members);
    }

    /**
     * 更新群公告（仅教师/管理员）
     */
    @PutMapping("/rooms/{roomId}/notice")
    public Result updateRoomNotice(HttpServletRequest request, @PathVariable Long roomId, @RequestBody Map<String, String> body) {
        User currentUser = getCurrentUser(request);
        if (currentUser.getRoleId() == null || currentUser.getRoleId() < 3) {
            return Result.badRequest("仅教师/管理员可修改群公告");
        }
        String notice = body.get("notice");
        ChatRoom room = chatMessageService.updateRoomNotice(roomId, notice);
        return Result.ok(room);
    }

    /**
     * 获取当前用户在某房间的设置
     */
    @GetMapping("/rooms/{roomId}/user-setting")
    public Result getUserRoomSetting(HttpServletRequest request, @PathVariable Long roomId) {
        User currentUser = getCurrentUser(request);
        UserRoomSetting setting = chatMessageService.getUserRoomSetting(currentUser.getId(), roomId);
        if (setting == null) {
            // 返回默认值
            Map<String, Object> defaultSetting = new HashMap<>();
            defaultSetting.put("isPinned", false);
            defaultSetting.put("isMuted", false);
            return Result.ok(defaultSetting);
        }
        return Result.ok(setting);
    }

    /**
     * 保存当前用户在某房间的设置（置顶/免打扰）
     */
    @PutMapping("/rooms/{roomId}/user-setting")
    public Result saveUserRoomSetting(HttpServletRequest request, @PathVariable Long roomId, @RequestBody Map<String, Boolean> body) {
        User currentUser = getCurrentUser(request);
        UserRoomSetting setting = chatMessageService.saveUserRoomSetting(
                currentUser.getId(), roomId,
                body.get("isPinned"),
                body.get("isMuted"));
        return Result.ok(setting);
    }

    /**
     * 上传聊天文件（图片/代码/文件）
     */
    @PostMapping("/upload")
    public Result uploadChatFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.badRequest("文件不能为空");

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }

        // 判断文件类型
        String messageType;
        if (java.util.Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp").contains(ext)) {
            messageType = "IMAGE";
        } else if (java.util.Set.of("c", "cpp", "h", "hpp", "java", "py", "js", "ts", "html", "css", "json", "xml", "sql", "sh", "go", "rs").contains(ext)) {
            messageType = "CODE";
        } else {
            messageType = "FILE";
        }

        // 校验大小
        long maxSize = messageType.equals("IMAGE") ? 5 * 1024 * 1024 : 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.badRequest(messageType.equals("IMAGE") ? "图片不能超过5MB" : "文件不能超过2MB");
        }

        try {
            String subDir = messageType.equals("IMAGE") ? "chat_images" : "chat_files";
            String baseDir = System.getProperty("user.dir") + "/uploads/" + subDir + "/";
            java.io.File dir = new java.io.File(baseDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            java.io.File dest = new java.io.File(baseDir + fileName);
            byte[] codeBytes = messageType.equals("CODE") ? file.getBytes() : null;
            file.transferTo(dest);

            String url = "/uploads/" + subDir + "/" + fileName;

            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("messageType", messageType);
            data.put("fileName", originalName);
            data.put("fileSize", file.getSize());
            data.put("extension", ext);
            if (codeBytes != null) {
                String codeContent = new String(codeBytes, StandardCharsets.UTF_8);
                int previewLimit = 60000;
                if (codeContent.length() > previewLimit) {
                    codeContent = codeContent.substring(0, previewLimit) + "\n\n/* 预览已截断，下载文件查看完整内容 */";
                }
                data.put("codeContent", codeContent);
            }
            return Result.ok(data);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 撤回消息（2分钟内）
     */
    @PostMapping("/messages/{messageId}/recall")
    public Result recallMessage(HttpServletRequest request, @PathVariable Long messageId) {
        User currentUser = getCurrentUser(request);
        try {
            ChatMessageResponse resp = chatMessageService.recallMessage(currentUser.getId(), messageId);
            return Result.ok(resp);
        } catch (RuntimeException e) {
            return Result.badRequest(e.getMessage());
        }
    }
}
