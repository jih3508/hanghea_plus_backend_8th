package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.entity.UserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /*
     * method: useCoupon
     * method: 쿠폰 사용 처리
     */
    public void useCoupon(Long userId, Long couponId) {
        UserCoupon userCoupon = repository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "사용가능 한 쿠폰 없습니다."));
        userCoupon.usedCoupon();
        repository.saveUserCoupon(userCoupon);
    }

    public void save(User user, Coupon coupon) {
        UserCoupon userCoupon = UserCoupon.builder()
                .user(user)
                .coupon(coupon)
                .build();
        repository.saveUserCoupon(userCoupon);
    }

    /*
     * method
     */
    public List<UserCoupon> getUserCoupons(Long userId) {
        return repository.findAllByUserId(userId);
    }


}
