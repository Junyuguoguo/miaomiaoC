package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.SimulationExamResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SimulationExamResultRepository extends JpaRepository<SimulationExamResult,Long> {
    @Query("select s from SimulationExamResult s where s.userId = :userIdLong and s.examId = :examIdLong")
    SimulationExamResult findSimulationExamResultByUserIdAndExamId(
            @Param("userIdLong") Long userIdLong,@Param("examIdLong") Long examIdLong);


    /**
     * 根据用户ID和考试ID更新考试结果（核心更新方法）
     */
    @Modifying // 标记为修改操作（非查询）
    @Transactional // 增删改必须加事务
    @Query("UPDATE SimulationExamResult ser " +
            "SET ser.examTotalScore = :examTotalScore, " +
            "    ser.isPassed = :isPassed, " +
            "    ser.updateTime = :updateTime " +
            "WHERE ser.id = :id")
    void updateSimulationExamResultByUserIdAndExamId(
            @Param("id") Long id,
            @Param("examTotalScore") BigDecimal examTotalScore, // 总分
            @Param("isPassed") Integer isPassed,   // 是否通过（1/0）
            @Param("updateTime") LocalDateTime updateTime // 更新时间
    );

    @Query("select s from SimulationExamResult s where s.userId = :userIdLong")
    List<SimulationExamResult> findSimulationExamResultByUserId(@Param("userIdLong") Long userIdLong);

    void deleteByExamId(Long examId);
}
