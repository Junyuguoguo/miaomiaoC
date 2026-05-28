package sen.yuhuang.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.UserCollectBank;

import java.util.List;

@Repository
public interface UserCollectBankRepository extends JpaRepository<UserCollectBank,Long> {

    /**
     * 查询用户收藏的题库记录（支持关键词搜索）
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 分页的收藏记录
     */
    @Query("SELECT u FROM UserCollectBank u " +
            "JOIN FETCH u.questionBank qb " +
            "WHERE u.userId = :userId " +
            "AND qb.status = 1 " +
            "AND (:keyword IS NULL OR :keyword = '' OR qb.title LIKE %:keyword% OR qb.desc LIKE %:keyword%) " +
            "ORDER BY u.createTime DESC")
    Page<UserCollectBank> findCollectedBanks(@Param("userId") Long userId,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);

    /**
     * 查询用户收藏的题库ID列表
     */
    @Query("SELECT uc.questionBankId FROM UserCollectBank uc WHERE uc.userId = :userId")
    List<Long> findBankIdsByUserId(@Param("userId") Long userId);

    @Modifying  // 必须添加
    @Transactional
        // 必须添加
    void deleteUserCollectBankByUserIdAndQuestionBankId(Long userId, Long bankId);}
