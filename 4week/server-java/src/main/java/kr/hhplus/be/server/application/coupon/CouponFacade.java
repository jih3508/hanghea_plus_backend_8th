package kr.hhplus.be.server.application.coupon;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponFacade {

    private final CouponService service;

    private final UserService userService;

    private final UserCouponService userCouponService;


    @Transactional
    public void issue(CouponIssueCommand command) {

        User user = userService.findById(command.getUserId());

        Coupon coupon = service.issueCoupon(command.getCouponId());

        userCouponService.save(user, coupon);

    }

    /*
     * method: getMeCoupons
     * description: 쿠폰 내것 조회
     */
    public List<CouponMeCommand>  getMeCoupons(Long userId) {
        return userCouponService.getUserCoupons(userId).stream()
                .map(CouponMeCommand::toCommand).toList();
    }
}
