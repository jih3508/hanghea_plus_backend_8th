package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository {

    Optional<Coupon> findById(long id);

    Coupon save(Coupon coupon);
}
