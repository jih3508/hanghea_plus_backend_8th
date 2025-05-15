package kr.hhplus.be.server.infrastructure.point;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointJpaRepository  extends JpaRepository<Point,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Point> findByUserId(Long userId);

    Optional<Point> findById(Long id);
}
