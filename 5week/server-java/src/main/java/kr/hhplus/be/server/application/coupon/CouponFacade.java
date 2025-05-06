package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.lock.DistributedLockType;
import kr.hhplus.be.server.common.lock.DistributedLockable;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponFacade {

    private final CouponService service;

    private final UserService userService;

    private final UserCouponService userCouponService;


    /*
     * method: issue
     * description: 쿠폰발급
     * Distributed lock applied with couponId as the key
     */
    @DistributedLockable(key = "'coupon:' + #command.couponId", lockType = DistributedLockType.REDISSON)
    @Transactional(rollbackFor =  Exception.class)
    public void issue(CouponIssueCommand command) {

        DomainUser user = userService.findById(command.getUserId());

        DomainCoupon coupon = service.issueCoupon(command.getCouponId());

        userCouponService.issue(user.getId(), coupon.getId());

    }

    /*
     * method: getMeCoupons
     * description: 쿠폰 내것 조회
     */
    @Transactional(readOnly = true)
    public List<CouponMeCommand>  getMeCoupons(Long userId) {
        return userCouponService.getUserCoupons(userId).stream()
                .map(CouponMeCommand::toCommand).toList();
    }
}
