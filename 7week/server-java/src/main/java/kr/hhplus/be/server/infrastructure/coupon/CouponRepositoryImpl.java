package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.model.UpdateCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository jpaRepository;

    private final CouponRedisRepository redisRepository;

    @Override
    public Optional<DomainCoupon> findById(long id) {
        return jpaRepository.findById(id).map(Coupon::toDomain);
    }

    @Override
    public DomainCoupon create(CreateCoupon createCoupon) {
        return jpaRepository.save(Coupon.createCoupon(createCoupon)).toDomain();
    }

    @Override
    public DomainCoupon update(UpdateCoupon updateCoupon) {
        Coupon coupon = jpaRepository.findById(updateCoupon.getCouponId()).get();
        coupon.setQuantity(updateCoupon.getQuantity());
        return jpaRepository.save(coupon).toDomain();
    }

    @Override
    public Boolean decreaseCoupon(long couponId) {
        return redisRepository.decreaseCoupon(couponId);
    }

    @Override
    public void increaseCoupon(long couponId) {
        redisRepository.increaseCoupon(couponId);
    }
}
