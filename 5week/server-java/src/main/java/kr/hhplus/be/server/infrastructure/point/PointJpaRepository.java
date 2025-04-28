package kr.hhplus.be.server.infrastructure.point;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointJpaRepository  extends JpaRepository<Point,Long> {

    Optional<Point> findByUserId(Long userId);

    Optional<Point> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Point p WHERE p.user.id = :userId")
    Optional<Point> findByUserIdWithLock(@Param("userId") Long userId);
}
