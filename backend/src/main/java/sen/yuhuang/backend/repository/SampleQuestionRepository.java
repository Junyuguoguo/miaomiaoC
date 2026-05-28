package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sen.yuhuang.backend.entity.SampleQuestion;

import java.util.List;

@Repository
public interface SampleQuestionRepository extends JpaRepository<SampleQuestion,Long> {

    @Query("select s from SampleQuestion s where s.questionId = :questionId")
    List<SampleQuestion> findSampleQuestionByQuestionId(@Param("questionId") Long questionId);

    void deleteByQuestionId(Long questionId);
}
