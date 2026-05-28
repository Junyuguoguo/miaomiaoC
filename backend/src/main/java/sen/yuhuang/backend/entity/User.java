package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "school", length = 30)
    private String school;

    @Column(name = "major", length = 30)
    private String major;

    @Column(name = "score", length = 10)
    private String score;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status", columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;  // 默认启用

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "vip_expire_time")
    private LocalDateTime vipExpireTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "avatar",length = 255)
    private String avatar;

    // ========== 辅助方法 ==========

    /**
     * 判断用户是否VIP
     */
    public boolean isVip() {
        return vipExpireTime != null && vipExpireTime.isAfter(LocalDateTime.now());
    }

    /**
     * 判断用户是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}