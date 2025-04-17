package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.infrastructure.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointJpaRepository  extends JpaRepository<Point,Long> {

    Optional<Point> findByUserId(Long userId);

    Optional<Point> findById(Long id);
}
