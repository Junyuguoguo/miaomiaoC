package sen.yuhuang.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.ChatMessageResponse;
import sen.yuhuang.backend.entity.ChatRoom;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.entity.UserRoomSetting;
import sen.yuhuang.backend.repository.UserRepository;
import sen.yuhuang.backend.service.ChatMessageService;

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
}
