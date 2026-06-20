package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sen.yuhuang.backend.entity.UserRoomSetting;
import java.util.List;
import java.util.Optional;

public interface UserRoomSettingRepository extends JpaRepository<UserRoomSetting, Long> {
    Optional<UserRoomSetting> findByUserIdAndRoomId(Long userId, Long roomId);
    List<UserRoomSetting> findByUserId(Long userId);
}
