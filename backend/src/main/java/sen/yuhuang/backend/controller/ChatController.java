package sen.yuhuang.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.ChatMessageResponse;
import sen.yuhuang.backend.entity.ChatRoom;
import sen.yuhuang.backend.entity.User;
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
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("房间名不能为空");
        }
        return Result.ok(chatMessageService.createRoom(name, description, college, currentUser.getId()));
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
}
