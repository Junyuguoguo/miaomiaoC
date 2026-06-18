package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sen.yuhuang.backend.entity.ChatRoomMember;

import java.util.List;
import java.util.Optional;

/**
 * 聊天房间成员数据访问层
 */
@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    /**
     * 查询房间的所有成员
     */
    List<ChatRoomMember> findByRoomId(Long roomId);

    /**
     * 查询用户加入的所有房间
     */
    List<ChatRoomMember> findByUserId(Long userId);

    /**
     * 查询用户在某个房间的成员信息
     */
    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    /**
     * 统计房间成员数
     */
    long countByRoomId(Long roomId);

    /**
     * 删除房间成员
     */
    void deleteByRoomIdAndUserId(Long roomId, Long userId);
}
