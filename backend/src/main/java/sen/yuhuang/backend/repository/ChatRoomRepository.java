package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sen.yuhuang.backend.entity.ChatRoom;

import java.util.List;

/**
 * 聊天房间数据访问层
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 查询所有公开房间
     */
    List<ChatRoom> findByRoomTypeAndIsActive(String roomType, Boolean isActive);

    /**
     * 查询用户加入的房间列表
     */
}
