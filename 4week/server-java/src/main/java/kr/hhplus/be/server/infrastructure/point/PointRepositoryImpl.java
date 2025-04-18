package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.UpdatePoint;
import kr.hhplus.be.server.infrastructure.point.entity.Point;
import kr.hhplus.be.server.domain.point.model.CreatePoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.infrastructure.user.UserJpaRepository;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository jpaRepository;

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<DomainPoint> findByUserId(Long userId) {

        return jpaRepository.findByUserId(userId)
                .map(Point::toDomain);
    }

    @Override
    public DomainPoint create(Long userId) {
        return userJpaRepository.findById(userId)
                .map(user ->  jpaRepository.save(Point.create(user)))
                .map(Point::toDomain)
                .orElse(null);
    }

    @Override
    public DomainPoint update(UpdatePoint updatePoint) {
        return jpaRepository.findById(updatePoint.getPointId())
                .map(point -> {
                    point.setPoint(updatePoint.getPoint());
                    return jpaRepository.save(point);
                })
                .map(Point::toDomain)
                .orElse(null);
    }

    @Override
    public void delete(Long userId) {
        jpaRepository.deleteById(userId);
    }

}
