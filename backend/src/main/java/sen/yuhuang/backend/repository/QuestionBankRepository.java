package sen.yuhuang.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.QuestionBank;

import java.util.List;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank,Long> {
    /**
     * 查询全部题库（支持关键词搜索和VIP筛选）
     * @param keyword 搜索关键词
     * @param vip VIP类型：null-全部，0-普通题库，1-VIP题库
     * @param pageable 分页参数
     */
    @Query("SELECT b FROM QuestionBank b WHERE b.status = 1 " +
            "AND (:vip IS NULL OR b.isVip = :vip) " +
            "AND (:keyword IS NULL OR :keyword = '' OR b.title LIKE %:keyword% OR b.desc LIKE %:keyword%)")
    Page<QuestionBank> findAllBanks(@Param("keyword") String keyword,
                                    Pageable pageable,
                                    @Param("vip") Integer vip);

    List<QuestionBank> findQuestionBankById(Long id);


    @Modifying
    @Transactional
    @Query("UPDATE QuestionBank b SET b.viewCount = :count WHERE b.id = :bankId")
    void updateViewCountById(@Param("bankId") Long bankId, @Param("count") int count);
}
