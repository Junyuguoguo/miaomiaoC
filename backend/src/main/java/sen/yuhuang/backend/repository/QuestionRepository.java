package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {


    @Query("select q from Question q where q.simulationExamId = :simulationExamId and q.sourceType = :type")
    List<Question> findQuestionBySimulationExamIdAndType(@Param("simulationExamId")Long simulationExamId,@Param("type") int type);

    @Query("select q from Question q where q.id = :questionIdLong")
    Question findQuestionById(@Param("questionIdLong") Long questionIdLong);

    @Query("select q from Question q where q.simulationExamId = :simulationExamId")
    List<Question> findQuestionBySimulationExamId(@Param("simulationExamId") Long simulationExamId);

    /**
     * 更新题目的做题人数
     * @param questionId 题目ID
     * @param count 做题人数
     */
    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.participantCount = :count WHERE q.id = :questionId")
    void updateParticipantCountByQuestionId(@Param("questionId") Long questionId, @Param("count") Integer count);

    List<Question> findQuestionByBankId(Long bankId);

}
