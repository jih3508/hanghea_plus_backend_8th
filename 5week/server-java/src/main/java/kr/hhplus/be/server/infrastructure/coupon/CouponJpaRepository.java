package kr.hhplus.be.server.infrastructure.coupon;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon,Long> {

    Optional<Coupon> findById(Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT c FROM Coupon c WHERE c.id = :id")
    Optional<Coupon> findByIdWithLock(@Param("id") long id);

    // 직접 SQL 업데이트 추가
    @Modifying
    @Query("UPDATE Coupon c SET c.quantity = c.quantity - 1 WHERE c.id = :id AND c.quantity > 0")
    int decrementCouponQuantity(@Param("id") Long id);
}
