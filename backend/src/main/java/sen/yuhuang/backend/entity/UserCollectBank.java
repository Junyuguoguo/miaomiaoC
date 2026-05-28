package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收藏题库实体类
 * 对应数据库表：user_collect_bank
 */
@Data
@Entity
@Table(name = "user_collect_bank",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_bank",
                        columnNames = {"user_id", "question_bank_id"})
        })
@AllArgsConstructor
@NoArgsConstructor
public class UserCollectBank {

    /**
     * 收藏ID（主键，自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户ID（外键）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 题库ID（外键）
     */
    @Column(name = "question_bank_id", nullable = false)
    private Long questionBankId;

    /**
     * 收藏时间
     */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 实体持久化前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }

    /**
     * 可选：关联题库实体（懒加载）
     * 用于需要查询收藏题库详情时使用
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id", insertable = false, updatable = false)
    private QuestionBank questionBank;

    /**
     * 可选：关联用户实体（懒加载）
     * 用于需要查询用户信息时使用
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}