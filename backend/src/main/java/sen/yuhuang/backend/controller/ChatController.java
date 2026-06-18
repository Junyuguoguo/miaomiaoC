package sen.yuhuang.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.dto.ChatMessageResponse;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.service.ChatMessageService;

import java.util.List;

/**
 * 聊天 HTTP 控制器
 * 
 * 处理历史消息查询、未读数统计等非实时操作
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    /**
     * 获取单聊历史消息
     * 
     * @param userId2 对方用户ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     */
    @GetMapping("/private/{userId2}")
    public Result<Page<ChatMessageResponse>> getPrivateMessages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ChatMessageResponse> messages = chatMessageService.getPrivateMessages(
                currentUser.getId(), userId2, page, size);
        
        return Result.success(messages);
    }

    /**
     * 获取群聊历史消息
     * 
     * @param roomId 房间ID
     * @param page 页码（从0开始）
     * @param size 每页大小
     */
    @GetMapping("/room/{roomId}")
    public Result<Page<ChatMessageResponse>> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<ChatMessageResponse> messages = chatMessageService.getRoomMessages(roomId, page, size);
        
        return Result.success(messages);
    }

    /**
     * 获取最近联系人列表
     */
    @GetMapping("/contacts")
    public Result<List<Long>> getRecentContacts(@AuthenticationPrincipal User currentUser) {
        List<Long> contacts = chatMessageService.getRecentContacts(currentUser.getId());
        return Result.success(contacts);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        long count = chatMessageService.getUnreadCount(currentUser.getId());
        return Result.success(count);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read")
    public Result<Void> markAsRead(@AuthenticationPrincipal User currentUser,
                                   @RequestBody List<Long> messageIds) {
        chatMessageService.markAsRead(currentUser.getId(), messageIds);
        return Result.success();
    }

    /**
     * 标记某个联系人的所有消息为已读
     */
    @PostMapping("/read-all/{contactId}")
    public Result<Void> markAllAsRead(@AuthenticationPrincipal User currentUser,
                                      @PathVariable Long contactId) {
        chatMessageService.markAllAsRead(currentUser.getId(), contactId);
        return Result.success();
    }
}
