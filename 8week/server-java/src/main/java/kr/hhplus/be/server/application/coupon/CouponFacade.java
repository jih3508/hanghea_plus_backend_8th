package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.LockStrategy;
import kr.hhplus.be.server.common.lock.LockType;
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
     */
    //@DistributedLock(key = "#command.getCouponId()", type = LockType.COUPON , strategy = LockStrategy.PUB_SUB_LOCK)
    @Transactional
    public void issue(CouponIssueCommand command) {

        // 쿠폰 개수 있지는지 먼저 조회
        service.checkCouponCounter(command.getCouponId());

        try {

            DomainUser user = userService.findById(command.getUserId());
            userCouponService.issue(user.getId(), command.getCouponId());

        }catch (RuntimeException e) {

            // 중간에 실패 하면 다시 원복 시킴
            service.resetCouponCounter(command.getCouponId());
        }

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
