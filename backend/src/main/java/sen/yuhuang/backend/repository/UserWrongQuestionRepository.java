package sen.yuhuang.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.UserWrongQuestion;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserWrongQuestionRepository extends JpaRepository<UserWrongQuestion,Long> {
    /**
     * 查询用户的错题记录（支持关键词搜索题库名称）
     * @param userId 用户ID
     * @param keyword 搜索关键词（搜索题库标题或描述）
     * @param pageable 分页参数
     * @return 分页的错题记录
     */
    @Query("SELECT uwq FROM UserWrongQuestion uwq " +
            "JOIN FETCH uwq.questionBank qb " +
            "JOIN FETCH uwq.question q " +
            "WHERE uwq.userId = :userId " +
            "AND (:keyword IS NULL OR :keyword = '' OR qb.title LIKE %:keyword% OR qb.desc LIKE %:keyword% OR q.questionDesc LIKE %:keyword%) " +
            "ORDER BY uwq.lastWrongTime DESC")
    Page<UserWrongQuestion> findWrongQuestionBanks(@Param("userId") Long userId,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);


    /**
     * 统计用户在某个题库中的错题数量
     */
    @Query("SELECT COUNT(wq) FROM UserWrongQuestion wq " +
            "WHERE wq.userId = :userId AND wq.questionBankId = :bankId")
    Integer countByUserIdAndBankId(@Param("userId") Long userId, @Param("bankId") Long bankId);

    /**
     * 统计用户每个题库的错题数量
     * 返回 Object[] 数组，第一个元素是题库ID，第二个元素是错题数量
     */
    @Query("SELECT wq.questionBankId, COUNT(wq) FROM UserWrongQuestion wq " +
            "WHERE wq.userId = :userId " +
            "GROUP BY wq.questionBankId")
    List<Object[]> countByUserIdGroupByBank(@Param("userId") Long userId);

    @Query("select u from UserWrongQuestion u where u.userId = :userId")
    List<UserWrongQuestion> findUserWrongQuestionByUserId(@Param("userId") Long userId);

    UserWrongQuestion findUserWrongQuestionsByUserIdAndQuestionId(Long userId, Long questionId);


    @Modifying
    @Transactional
    @Query("UPDATE UserWrongQuestion u SET u.code = :code, u.updateTime = :now WHERE u.id = :id")
    void updateCodeAndTimeById(@Param("id") Long id,
                               @Param("code") String code,
                               @Param("now") LocalDateTime now);

    void deleteUserWrongQuestionsByQuestionId(Long questionId);
}
