package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponFacade {

    private final CouponService service;

    private final UserService userService;

    private final UserCouponService userCouponService;

    private final CouponService couponService;

    public void issue(CouponIssueCommand command) {

        User user = userService.findById(command.getUserId());

        Coupon coupon = couponService.issueCoupon(command.getCouponId());

        userCouponService.save(user, coupon);

    }
}
