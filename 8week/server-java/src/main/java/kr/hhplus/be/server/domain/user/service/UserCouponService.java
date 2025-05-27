package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.model.CreateUserCoupon;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.model.UpdateUserCoupon;
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
    public DomainUserCoupon getUseCoupon(Long userId, Long couponId) {

        return repository.findByUserIdAndCouponIdLock(userId, couponId)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "사용가능 한 쿠폰 없습니다."));
    }

    /*
     * method: useCoupon
     * method: 쿠폰 사용 처리
     */
    public void useCoupon(Long userId, Long couponId) {
        DomainUserCoupon userCoupon = this.getUseCoupon(userId, couponId);
        userCoupon.usedCoupon();
        repository.updateUserCoupon(new UpdateUserCoupon(userId, couponId, userCoupon.getIsUsed()));
    }


    public void issue(Long userId, Long couponId) {
        repository.create(new CreateUserCoupon(userId, couponId));
    }

    /*
     * method
     */
    public List<DomainUserCoupon> getUserCoupons(Long userId) {
        return repository.findAllByUserId(userId);
    }


}
