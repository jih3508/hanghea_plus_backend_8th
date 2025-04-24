package kr.hhplus.be.server.infrastructure.user;

import kr.hhplus.be.server.infrastructure.user.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon,Long> {

    Optional<UserCoupon> findById(Long id);

    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    List<UserCoupon> findByUserId(Long userId);
}
