package kr.hhplus.be.server.infrastructure.user;

import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public User save(CreateUser createUser) {
        User user = User.create(createUser);
        return userJpaRepository.save(user);
    }
}
