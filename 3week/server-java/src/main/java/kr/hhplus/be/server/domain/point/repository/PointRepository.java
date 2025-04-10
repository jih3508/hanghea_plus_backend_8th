package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.entity.Point;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository {

    Optional<Point>  findByUserId(Long userId);

    Point save(Point point);

}
