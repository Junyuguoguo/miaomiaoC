package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sen.yuhuang.backend.entity.VipKey;

import java.util.List;
import java.util.Optional;

@Repository
public interface VipKeyRepository extends JpaRepository<VipKey, Long> {

    Optional<VipKey> findByKeyCode(String keyCode);

    List<VipKey> findByCreatedByOrderByCreateTimeDesc(Long createdBy);

    long countByCreatedByAndStatus(Long createdBy, Integer status);
}
