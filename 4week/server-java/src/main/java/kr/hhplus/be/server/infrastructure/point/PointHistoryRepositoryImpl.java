package kr.hhplus.be.server.infrastructure.point;


import kr.hhplus.be.server.domain.point.model.CreatePointHistory;
import kr.hhplus.be.server.domain.point.model.DomainPointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.infrastructure.point.entity.PointHistory;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository JpaRepository;

    private final UserJpaRepository userJpaRepository;

    @Override
    public void create(CreatePointHistory pointHistory) {
        userJpaRepository.findById(pointHistory.getUserId())
                .ifPresent(user -> {
                    JpaRepository.save(PointHistory.create(pointHistory, user));
                });
    }

    @Override
    public Optional<DomainPointHistory> findByAtLast() {
        return JpaRepository.findTopByOrderByCreateDateTimeDesc().map(PointHistory::toDomain)
                .map(Optional::of)
                .orElse(Optional.empty());
    }

    @Override
    public List<DomainPointHistory> findByUserId(Long userId) {
        return JpaRepository.findAlByUserId();
    }
}
