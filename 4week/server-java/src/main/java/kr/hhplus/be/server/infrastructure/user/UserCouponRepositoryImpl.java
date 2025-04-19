package kr.hhplus.be.server.infrastructure.user;

import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.model.UpdateUserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import kr.hhplus.be.server.infrastructure.coupon.CouponJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.infrastructure.user.entity.UserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private UserCouponJpaRepository repository;

    private CouponJpaRepository couponRepository;



    @Override
    public Optional<DomainUserCoupon> findByUserIdAndCouponId(Long userId, Long couponId) {


        return repository.findByUserIdAndCouponId(userId, couponId)
                .map( userCoupon -> {
                    Coupon coupon = couponRepository.findById(couponId).get();
                    return DomainUserCoupon.of(userCoupon, coupon);
                });
    }

    @Override
    public void create(CreateUserCoupon userCoupon) {
        repository.save(UserCoupon.create(userCoupon));
    }

    @Override
    public void updateUserCoupon(UpdateUserCoupon updateUserCoupon) {

        repository.findByUserIdAndCouponId(updateUserCoupon.getUserId(), updateUserCoupon.getCouponId())
                .ifPresent(userCoupon -> {
                    userCoupon.setIsUsed(updateUserCoupon.getIsUsed());
                   repository.save(userCoupon);
                });
    }

    @Override
    public List<DomainUserCoupon> findAllByUserId(Long userId) {

        return repository.findByUserId(userId).stream()
                .map(userCoupon -> {
                        Coupon coupon = couponRepository.findById(userCoupon.getCouponId()).get();
                        return DomainUserCoupon.of(userCoupon, coupon);
                    }).toList();
    }
}
