package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 根据学院查询可见房间（包括全校房间和本学院房间）
     */
    @Query("SELECT r FROM ChatRoom r WHERE r.college IS NULL OR r.college = :college ORDER BY r.id ASC")
    List<ChatRoom> findVisibleRooms(@Param("college") String college);

    /**
     * 查询所有房间（管理员用）
     */
    @Query("SELECT r FROM ChatRoom r ORDER BY r.id ASC")
    List<ChatRoom> findAllRooms();
}
