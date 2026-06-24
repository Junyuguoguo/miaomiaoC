package sen.yuhuang.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.TeacherInviteCode;

import java.time.LocalDateTime;

@Repository
public interface TeacherInviteCodeRepository extends JpaRepository<TeacherInviteCode, Long> {

    TeacherInviteCode findByCode(String code);

    Page<TeacherInviteCode> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Page<TeacherInviteCode> findByCollegeOrderByCreateTimeDesc(String college, Pageable pageable);

    Page<TeacherInviteCode> findByStatusAndCollegeOrderByCreateTimeDesc(Integer status, String college, Pageable pageable);

    Page<TeacherInviteCode> findAllByOrderByCreateTimeDesc(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE TeacherInviteCode t SET t.status = 2 WHERE t.id = :id AND t.status = 0")
    int revokeById(Long id);

    @Transactional
    @Modifying
    @Query("UPDATE TeacherInviteCode t SET t.status = :status, t.usedBy = :usedBy, t.usedAt = :usedAt WHERE t.id = :id")
    int updateUsage(Long id, Integer status, Long usedBy, LocalDateTime usedAt);

    @Query("SELECT COUNT(t) FROM TeacherInviteCode t WHERE t.status = 0 AND t.expiresAt > :now")
    long countActive(LocalDateTime now);
}
