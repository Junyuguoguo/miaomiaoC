package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.TestSample;

import java.util.List;

@Repository
public interface TestSampleRepository extends JpaRepository<TestSample,Integer> {

    @Query("select t from TestSample t where t.questionId = :questionIdLong")
    List<TestSample> findAllByQuestionId(@Param("questionIdLong") Long questionIdLong);

    @Query("select t from TestSample t where t.id = :testSampleId")
    TestSample findTestSampleById(@Param("testSampleId") Long testSampleId);

    void deleteByQuestionId(Long questionId);
}
