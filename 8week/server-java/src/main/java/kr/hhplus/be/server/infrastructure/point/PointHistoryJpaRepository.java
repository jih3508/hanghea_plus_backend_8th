package kr.hhplus.be.server.infrastructure.point;

import kr.hhplus.be.server.infrastructure.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, Long> {

    Optional<PointHistory> findById(Long id);

    Optional<PointHistory> findTopByOrderByCreateDateTimeDesc();

    List<PointHistory> findAllByUserId(Long userId);
}
