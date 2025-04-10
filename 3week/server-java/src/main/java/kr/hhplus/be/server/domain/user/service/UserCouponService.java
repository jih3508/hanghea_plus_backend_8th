package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.user.entity.UserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository repository;


    /*
     * method: getUseCoupon
     * description: 사용가능한 쿠폰 가져오기
     */
    public Coupon getUseCoupon(Long userId, Long couponId) {
        UserCoupon userCoupon = repository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "사용가능 한 쿠폰 없습니다."));

        return userCoupon.getCoupon();
    }
}
