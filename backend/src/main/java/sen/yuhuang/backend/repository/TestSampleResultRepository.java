package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.TestSampleResult;

import java.util.List;

@Repository
public interface TestSampleResultRepository extends JpaRepository<TestSampleResult,Long> {


    @Query("select t from TestSampleResult t where t.userId = :userIdLong and t.questionId = :questionIdLong")
    List<TestSampleResult> findTestSampleResultByUserIdAndQuestionId(@Param("userIdLong") Long userIdLong,
                                                                     @Param("questionIdLong") Long questionIdLong);

    @Modifying
    @Query("delete from TestSampleResult t where t.userId = :userIdLong and t.questionId = :questionIdLong")
    void deleteTestSampleResultByUserIdAndQuestionId(@Param("userIdLong") Long userIdLong,
                                                     @Param("questionIdLong") Long questionIdLong);
    @Modifying
    @Transactional
    @Query("delete from TestSampleResult t where t.testSampleId in :testSampleIds")
    void deleteByTestSampleIds(@Param("testSampleIds") List<Long> testSampleIds);

    void deleteTestSampleResultsByQuestionId(Long questionId);

    void deleteByExamId(Long examId);
}
