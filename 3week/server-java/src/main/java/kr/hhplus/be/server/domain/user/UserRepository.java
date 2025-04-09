package kr.hhplus.be.server.domain.user;

import kr.hhplus.be.server.domain.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> findById(Long id);
}
