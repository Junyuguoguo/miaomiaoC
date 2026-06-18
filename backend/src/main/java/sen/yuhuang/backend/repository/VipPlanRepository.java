package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sen.yuhuang.backend.entity.VipPlan;

import java.util.List;

@Repository
public interface VipPlanRepository extends JpaRepository<VipPlan, Long> {
    List<VipPlan> findByEnabledOrderBySortOrderAscIdAsc(Integer enabled);

    List<VipPlan> findAllByOrderBySortOrderAscIdAsc();
}
