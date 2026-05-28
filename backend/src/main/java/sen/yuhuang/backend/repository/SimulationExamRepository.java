package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.SimulationExam;

import java.util.List;

@Repository
public interface SimulationExamRepository extends JpaRepository<SimulationExam,Long> {
    @Query("select s from SimulationExam s where s.isVipOnly = :isVIP")
    List<SimulationExam> getExamList(@Param("isVIP") int isVIP);

    @Query("select s from SimulationExam s")
    List<SimulationExam> getExamList();

    @Query("select s from SimulationExam s where s.id = :examId")
    SimulationExam getExamByExamId(@Param("examId") Long examId);

    /**
     * 更新考试的参与人数
     * @param examId 考试ID
     * @param count 参与人数
     */
    @Modifying
    @Transactional
    @Query("UPDATE SimulationExam se SET se.participantCount = :count WHERE se.id = :examId")
    void updateParticipantCountByExamId(@Param("examId") Long examId, @Param("count") Integer count);
}
