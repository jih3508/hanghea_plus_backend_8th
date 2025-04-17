package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon,Long> {

    Optional<Coupon> findById(Long id);
}
