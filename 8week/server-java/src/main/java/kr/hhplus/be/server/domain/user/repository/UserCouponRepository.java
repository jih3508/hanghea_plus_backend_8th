package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.model.UpdateUserCoupon;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository {

    Optional<DomainUserCoupon> findByUserIdAndCouponIdLock(Long userId, Long couponId);

    void create(CreateUserCoupon userCoupon);

    void updateUserCoupon(UpdateUserCoupon updateUserCoupon);

    List<DomainUserCoupon> findAllByUserId(Long userId);

    Optional<DomainUserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
}
