package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.entity.UserCoupon;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCouponRepository {

    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    void saveUserCoupon(UserCoupon userCoupon);
}
