package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository repository;

    public Coupon getCoupon(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "쿠폰이 없습니다."));
    }


    public Coupon issueCoupon(Long id) {
        Coupon coupon = this.getCoupon(id);
        coupon.issueCoupon();
        repository.save(coupon);
        return coupon;
    }

}
