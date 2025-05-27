package kr.hhplus.be.server.infrastructure.user;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.user.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCoupon,Long> {

    Optional<UserCoupon> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select uc from UserCoupon uc where uc.userId = :userId and uc.couponId = :couponId")
    Optional<UserCoupon> findByUserIdAndCouponIdLock(Long userId, Long couponId);

    List<UserCoupon> findByUserId(Long userId);

    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);
}
