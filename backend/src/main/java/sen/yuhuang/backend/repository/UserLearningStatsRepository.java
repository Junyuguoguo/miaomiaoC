package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.UserLearningStats;

import java.math.BigDecimal;

@Repository
public interface UserLearningStatsRepository extends JpaRepository<UserLearningStats,Long> {

    @Query("select u from UserLearningStats u where u.userId = :userIdLong")
    UserLearningStats findUserLearningStatsByUserId(@Param("userIdLong") Long userIdLong);

    /**
     * 根据ID更新考试次数
     * @param statsId 统计记录ID
     * @param examCount 新的考试次数
     */
    @Modifying  // 标记为修改操作（增删改）
    @Transactional  // 必须加事务，否则会报 TransactionRequiredException
    @Query("UPDATE UserLearningStats u SET u.examCount = :examCount WHERE u.id = :statsId")
    void updateExamCountById(@Param("statsId") Long statsId, @Param("examCount") int examCount);


    /**
     * 根据ID更新题目数量、成功数量、通过率
     * @param id 主键ID
     * @param questionCount 题目总数
     * @param successCount 成功完成数
     * @param questionPassRate 通过率
     */
    @Modifying  // 标记为修改操作（增删改）
    @Transactional  // 事务注解（必填，否则报TransactionRequiredException）
    @Query("UPDATE UserLearningStats u " +
            "SET u.questionCount = :questionCount, " +
            "    u.successCount = :successCount, " +
            "    u.questionPassRate = :questionPassRate " +
            "WHERE u.id = :id")
    void updateCountAndRateById(
            @Param("id") Long id,
            @Param("questionCount") Integer questionCount,
            @Param("successCount") Integer successCount,
            @Param("questionPassRate") BigDecimal questionPassRate
    );
}
