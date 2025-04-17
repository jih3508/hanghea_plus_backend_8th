package kr.hhplus.be.server.application.coupon;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.model.DomainUser;
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

        DomainUser user = userService.findById(command.getUserId());

        DomainCoupon coupon = service.issueCoupon(command.getCouponId());

        userCouponService.issue(user.getId(), coupon.getId());

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
