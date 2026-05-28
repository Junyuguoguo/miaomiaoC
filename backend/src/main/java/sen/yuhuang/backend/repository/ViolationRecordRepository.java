package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import sen.yuhuang.backend.entity.ViolationRecord;

@Component
public interface ViolationRecordRepository extends JpaRepository<ViolationRecord,Long> {
    void deleteByExamId(Long examId);
}
