package kr.hhplus.be.server.infrastructure.user;

import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.DomainUser;
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
    public Optional<DomainUser> findById(Long id) {
        Optional<User> user = userJpaRepository.findById(id);

        return userJpaRepository.findById(id)
                .map(User::toDomain);
    }

    @Override
    public User create(CreateUser createUser) {
        User user = User.create(createUser);
        return userJpaRepository.save(user);
    }
}
