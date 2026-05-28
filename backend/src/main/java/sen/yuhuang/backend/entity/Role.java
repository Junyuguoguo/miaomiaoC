package sen.yuhuang.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色实体类
 * 对应数据库表：role
 */
@Data
@Entity
@Table(name = "role")
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;  // 角色ID

    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String roleCode;  // 角色编码

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;  // 角色名称

    @Column(name = "description", length = 200)
    private String description;  // 角色描述

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;  // 创建时间

    /**
     * 实体持久化前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}