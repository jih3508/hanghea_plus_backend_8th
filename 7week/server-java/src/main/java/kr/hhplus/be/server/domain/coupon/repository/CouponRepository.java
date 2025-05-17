package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.model.UpdateCoupon;

import java.util.Optional;

public interface CouponRepository {

    Optional<DomainCoupon> findById(long id);

    DomainCoupon create(CreateCoupon coupon);

    DomainCoupon update(UpdateCoupon coupon);

    Boolean decreaseCoupon(long couponId);

    void increaseCoupon(long couponId);

}
