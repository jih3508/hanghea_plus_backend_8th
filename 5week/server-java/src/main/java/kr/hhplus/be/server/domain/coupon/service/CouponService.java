package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.model.UpdateCoupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository repository;

    public DomainCoupon getCoupon(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "쿠폰이 없습니다."));
    }


    public DomainCoupon issueCoupon(Long couponId) {
        DomainCoupon coupon = this.getCoupon(couponId);
        coupon.issueCoupon();
        return repository.update(new UpdateCoupon(coupon.getId(), coupon.getQuantity()));
    }

}
