package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<DomainUser> findById(Long id);

    User save(CreateUser user);
}
