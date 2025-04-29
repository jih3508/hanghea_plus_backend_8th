package kr.hhplus.be.server.infrastructure.user;

import kr.hhplus.be.server.infrastructure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    @Override
    Optional<User> findById(Long id);
}
