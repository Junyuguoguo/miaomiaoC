package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sen.yuhuang.backend.entity.InterviewCollege;

import java.util.List;

@Repository
public interface InterviewCollegeRepository extends JpaRepository<InterviewCollege, Long> {

    List<InterviewCollege> findAllByOrderBySortOrderAsc();

    List<InterviewCollege> findByEnabledOrderBySortOrderAsc(Integer enabled);

    InterviewCollege findByName(String name);
}
