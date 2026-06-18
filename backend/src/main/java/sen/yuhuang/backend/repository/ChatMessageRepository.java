package sen.yuhuang.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sen.yuhuang.backend.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息数据访问层
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 查询单聊消息（分页）
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1) " +
           "ORDER BY m.createTime DESC")
    Page<ChatMessage> findPrivateMessages(Long userId1, Long userId2, Pageable pageable);

    /**
     * 查询群聊消息（分页）
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId ORDER BY m.createTime DESC")
    Page<ChatMessage> findRoomMessages(Long roomId, Pageable pageable);

    /**
     * 查询用户最近的单聊对象列表
     */
    @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END as contactId " +
           "FROM ChatMessage m WHERE m.roomId IS NULL AND (m.senderId = :userId OR m.receiverId = :userId) " +
           "ORDER BY m.createTime DESC")
    List<Long> findRecentContacts(Long userId);

    /**
     * 统计未读消息数
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiverId = :userId AND m.isRead = false")
    long countUnreadMessages(Long userId);

    /**
     * 批量标记消息为已读
     */
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiverId = :userId AND m.id IN :messageIds")
    void markAsRead(Long userId, List<Long> messageIds);
}
