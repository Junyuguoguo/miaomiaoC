package sen.yuhuang.backend.repository;

import org.springframework.data.jpa.repository.Query;
import sen.yuhuang.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


}