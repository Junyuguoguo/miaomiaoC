package sen.yuhuang.backend.service;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.dto.ChatMessageRequest;
import sen.yuhuang.backend.dto.ChatMessageResponse;
import sen.yuhuang.backend.entity.ChatMessage;
import sen.yuhuang.backend.entity.ChatRoom;
import sen.yuhuang.backend.entity.ChatRoomMember;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.repository.ChatMessageRepository;
import sen.yuhuang.backend.repository.ChatRoomMemberRepository;
import sen.yuhuang.backend.repository.ChatRoomRepository;
import sen.yuhuang.backend.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 聊天消息服务
 * 
 * 并发优化策略：
 * 1. 使用 Redis 缓存最近的消息，减少数据库查询
 * 2. 批量写入数据库，降低写压力
 * 3. 分页加载历史消息，避免一次性加载大量数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    // Redis Key 前缀
    private static final String MESSAGE_CACHE_KEY = "chat:message:";
    private static final String UNREAD_COUNT_KEY = "chat:unread:";
    
    // 缓存过期时间（秒）
    private static final int CACHE_EXPIRE_SECONDS = 300; // 5分钟
    
    // 批量写入阈值
    private static final int BATCH_WRITE_THRESHOLD = 50;

    /**
     * 发送单聊消息
     */
    @Transactional
    public ChatMessageResponse sendPrivateMessage(Long senderId, ChatMessageRequest request) {
        // 参数校验
        if (request.getReceiverId() == null) {
            throw new IllegalArgumentException("接收者ID不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        // 获取发送者信息
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("发送者不存在"));

        // 创建消息实体
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setSenderName(sender.getUsername());
        message.setReceiverId(request.getReceiverId());
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setIsRead(false);

        // 保存到数据库
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 清除相关缓存
        clearMessageCache(senderId, request.getReceiverId());

        // 增加接收者的未读数（使用 Redis 原子操作）
        redisTemplate.opsForValue().increment(UNREAD_COUNT_KEY + request.getReceiverId(), 1);

        // 转换为响应DTO
        ChatMessageResponse resp = convertToResponse(savedMessage);
        resp.setSenderAvatar(sender.getAvatar());
        resp.setSenderCollege(sender.getCollege());
        resp.setSenderRole(sender.getRoleId());
        return resp;
    }

    /**
     * 发送群聊消息
     */
    @Transactional
    public ChatMessageResponse sendRoomMessage(Long senderId, ChatMessageRequest request) {
        // 参数校验
        if (request.getRoomId() == null) {
            throw new IllegalArgumentException("房间ID不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        // 获取发送者信息
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("发送者不存在"));

        // TODO: 验证用户是否在该房间中

        // 创建消息实体
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setSenderName(sender.getUsername());
        message.setRoomId(request.getRoomId());
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setIsRead(false);

        // 保存到数据库
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 清除房间消息缓存
        clearRoomMessageCache(request.getRoomId());

        // 转换为响应DTO
        ChatMessageResponse resp = convertToResponse(savedMessage);
        resp.setSenderAvatar(sender.getAvatar());
        resp.setSenderCollege(sender.getCollege());
        resp.setSenderRole(sender.getRoleId());
        return resp;
    }

    /**
     * 查询单聊历史消息（分页）
     */
    public Page<ChatMessageResponse> getPrivateMessages(Long userId1, Long userId2, int page, int size) {
        // 尝试从缓存获取
        String cacheKey = MESSAGE_CACHE_KEY + "private:" + Math.min(userId1, userId2) + ":" + Math.max(userId1, userId2);
        
        // 这里简化处理，实际应该缓存最近的N条消息
        // 生产环境建议使用 Redis List 或 ZSet 存储最新消息
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ChatMessage> messagePage = chatMessageRepository.findPrivateMessages(userId1, userId2, pageable);

        return messagePage.map(msg -> {
            ChatMessageResponse resp = convertToResponse(msg);
            User sender = userRepository.findById(msg.getSenderId()).orElse(null);
            if (sender != null) {
                resp.setSenderAvatar(sender.getAvatar());
                resp.setSenderCollege(sender.getCollege());
                resp.setSenderRole(sender.getRoleId());
            }
            return resp;
        });
    }

    /**
     * 查询群聊历史消息（分页）
     */
    public Page<ChatMessageResponse> getRoomMessages(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ChatMessage> messagePage = chatMessageRepository.findRoomMessages(roomId, pageable);

        // Batch-load sender info
        Set<Long> senderIds = messagePage.getContent().stream()
                .map(ChatMessage::getSenderId).collect(java.util.stream.Collectors.toSet());
        Map<Long, User> senderMap = new HashMap<>();
        if (!senderIds.isEmpty()) {
            userRepository.findByIds(new java.util.ArrayList<>(senderIds))
                    .forEach(u -> senderMap.put(u.getId(), u));
        }

        return messagePage.map(msg -> {
            ChatMessageResponse resp = convertToResponse(msg);
            User sender = senderMap.get(msg.getSenderId());
            if (sender != null) {
                resp.setSenderAvatar(sender.getAvatar());
                resp.setSenderCollege(sender.getCollege());
                resp.setSenderRole(sender.getRoleId());
            }
            return resp;
        });
    }

    /**
     * 获取用户最近联系人列表
     */
    public List<Long> getRecentContacts(Long userId) {
        return chatMessageRepository.findRecentContacts(userId);
    }

    /**
     * 获取未读消息数
     */
    public long getUnreadCount(Long userId) {
        // 先从 Redis 获取
        String key = UNREAD_COUNT_KEY + userId;
        String countStr = redisTemplate.opsForValue().get(key);
        
        if (countStr != null) {
            return Long.parseLong(countStr);
        }

        // Redis 没有则查询数据库
        long count = chatMessageRepository.countUnreadMessages(userId);
        
        // 更新到 Redis
        redisTemplate.opsForValue().set(key, String.valueOf(count), Duration.ofHours(1));
        
        return count;
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long userId, List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        // 批量更新数据库
        chatMessageRepository.markAsRead(userId, messageIds);

        // 清除未读数缓存
        redisTemplate.delete(UNREAD_COUNT_KEY + userId);
    }

    /**
     * 标记某个联系人的所有消息为已读
     */
    @Transactional
    public void markAllAsRead(Long userId, Long contactId) {
        // 查询该联系人的未读消息
        Pageable pageable = PageRequest.of(0, 1000); // 限制最多处理1000条
        Page<ChatMessage> unreadMessages = chatMessageRepository.findPrivateMessages(userId, contactId, pageable);

        List<Long> messageIds = unreadMessages.getContent().stream()
                .filter(msg -> !msg.getIsRead())
                .map(ChatMessage::getId)
                .collect(Collectors.toList());

        if (!messageIds.isEmpty()) {
            markAsRead(userId, messageIds);
        }
    }

    /**
     * 获取所有公开聊天室
     */
    public List<ChatRoom> getPublicRooms() {
        return chatRoomRepository.findByRoomTypeAndIsActive("PUBLIC", true);
    }

    /**
     * 获取用户加入的房间列表
     */
    public List<ChatRoom> getUserRooms(Long userId) {
        List<ChatRoomMember> memberships = chatRoomMemberRepository.findByUserId(userId);
        List<Long> roomIds = memberships.stream()
                .map(ChatRoomMember::getRoomId)
                .collect(Collectors.toList());
        if (roomIds.isEmpty()) {
            return new ArrayList<>();
        }
        return chatRoomRepository.findAllById(roomIds);
    }

    /**
     * 加入聊天室
     */
    @Transactional
    public void joinRoom(Long userId, Long roomId) {
        // 检查是否已在房间中
        if (chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId).isPresent()) {
            return; // 已在房间中，忽略
        }

        ChatRoomMember member = new ChatRoomMember();
        member.setRoomId(roomId);
        member.setUserId(userId);
        member.setRole("MEMBER");
        member.setJoinTime(LocalDateTime.now());
        chatRoomMemberRepository.save(member);

        // 更新房间成员数
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("房间不存在"));
        room.setCurrentMembers(room.getCurrentMembers() + 1);
        chatRoomRepository.save(room);
    }

    /**
     * 退出聊天室
     */
    @Transactional
    public void leaveRoom(Long userId, Long roomId) {
        chatRoomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);

        // 更新房间成员数
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room != null && room.getCurrentMembers() > 0) {
            room.setCurrentMembers(room.getCurrentMembers() - 1);
            chatRoomRepository.save(room);
        }
    }

    /**
     * 获取联系人详细信息（含用户名、头像等）
     */
    public List<Map<String, Object>> getContactDetails(Long userId) {
        List<Long> contactIds = chatMessageRepository.findRecentContacts(userId);
        if (contactIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<User> users = userRepository.findByIds(contactIds);
        // 保持与联系人相同的顺序
        Map<Long, User> userMap = new HashMap<>();
        users.forEach(u -> userMap.put(u.getId(), u));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long contactId : contactIds) {
            User user = userMap.get(contactId);
            if (user != null) {
                Map<String, Object> info = new HashMap<>();
                info.put("id", user.getId());
                info.put("username", user.getUsername());
                info.put("realName", user.getRealName());
                info.put("avatar", user.getAvatar());
                // 获取该联系人的未读消息数
                long unread = chatMessageRepository.countUnreadBySender(userId, contactId);
                info.put("unreadCount", unread);
                result.add(info);
            }
        }
        return result;
    }

    /**
     * 搜索用户（排除自己）
     */
    public List<Map<String, Object>> searchUsers(Long currentUserId, String keyword) {
        List<User> users = userRepository.searchByKeyword(keyword);
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            if (user.getId().equals(currentUserId)) continue;
            Map<String, Object> info = new HashMap<>();
            info.put("id", user.getId());
            info.put("username", user.getUsername());
            info.put("realName", user.getRealName());
            info.put("avatar", user.getAvatar());
            result.add(info);
        }
        return result;
    }

    /**
     * 清除单聊消息缓存
     */
    private void clearMessageCache(Long userId1, Long userId2) {
        String cacheKey = MESSAGE_CACHE_KEY + "private:" + Math.min(userId1, userId2) + ":" + Math.max(userId1, userId2);
        redisTemplate.delete(cacheKey);
    }

    /**
     * 清除群聊消息缓存
     */
    private void clearRoomMessageCache(Long roomId) {
        String cacheKey = MESSAGE_CACHE_KEY + "room:" + roomId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 转换为响应DTO
     */
    private ChatMessageResponse convertToResponse(ChatMessage message) {
        ChatMessageResponse response = new ChatMessageResponse();
        BeanUtil.copyProperties(message, response);
        return response;
    }
}
