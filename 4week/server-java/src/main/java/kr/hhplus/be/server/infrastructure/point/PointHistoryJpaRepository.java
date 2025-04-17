package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.infrastructure.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {
}
