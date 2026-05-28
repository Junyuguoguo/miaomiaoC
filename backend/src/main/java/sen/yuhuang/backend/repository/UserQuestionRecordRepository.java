package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.UserQuestionRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface UserQuestionRecordRepository extends JpaRepository<UserQuestionRecord,Integer> {

    @Query("select u from UserQuestionRecord u where u.questionId = :questionIdLong")
    UserQuestionRecord findQuestionRecordById(@Param("questionIdLong") Long questionIdLong);

    // 这是你的Repository接口中的方法
    @Modifying
    @Transactional  // 更新操作必须加事务注解
    @Query("UPDATE UserQuestionRecord q SET q.score = :userScore, q.updatedAt = :now ,q.code = :code WHERE q.id = :id")
    void updateQuestionRecordById(
            @Param("id") Long id,
            @Param("userScore") BigDecimal userScore,
            @Param("now") LocalDateTime now,
            @Param("code")  String code
    );

    @Query("select u from UserQuestionRecord u where u.userId = :userId and u.questionId = :questionId")
    UserQuestionRecord findQuestionRecordByUserIdAndQuestionId(@Param("userId") Long userIdLong,
                                                               @Param("questionId") Long id);

    void deleteUserQuestionRecordsByQuestionId(Long questionId);
}
