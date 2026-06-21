package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    @Query("select u from User u where u.username = :userName")
    User findUserByUsername(@Param("userName") String userName);

    @Query("select u from User u where u.email = :email")
    User findUserByEmail(@Param("email") String email);

    @Query("select u from User u where u.id = :userId")
    User findUserByUserId(@Param("userId") Long userId);

    @Query("select u from User u where u.username like %:keyword% or u.realName like %:keyword%")
    java.util.List<User> searchByKeyword(@Param("keyword") String keyword);

    @Query("select u from User u where u.id in :ids")
    java.util.List<User> findByIds(@Param("ids") java.util.List<Long> ids);

    // 修复后的更新方法（核心）
    @Transactional // 必须加：事务注解（public 方法才生效）
    @Modifying    // 必须加：标记是修改操作
    @Query("UPDATE User u SET " +
            "u.avatar = :avatar, " +
            "u.email = :email, " +
            "u.major = :major, " +
            "u.phone = :phone, " +
            "u.realName = :realName, " + // 确保与实体类属性名一致（比如实体类是 realName 而非 real_name）
            "u.school = :school, " +
            "u.score = :score, " +
            "u.college = :college " +
            "WHERE u.id = :userId") // userId 类型要匹配：如果实体类 id 是 Long，这里参数也用 Long
    void updateUserByUserId(
            @Param("userId") Long userId, // 建议改 Long：数据库 bigint 对应，避免 String 转换问题
            @Param("avatar") String avatar,
            @Param("email") String email,
            @Param("major") String major,
            @Param("phone") String phone,
            @Param("realName") String realName,
            @Param("school") String school,
            @Param("score") String score,
            @Param("college") String college
    );

}