package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.LockStrategy;
import kr.hhplus.be.server.common.lock.LockType;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.service.CouponRedisService;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponFacadeRedis {

    private final CouponService couponService;
    private final CouponRedisService couponRedisService;
    private final UserService userService;
    private final UserCouponService userCouponService;

    /**
     * Redis를 활용한 비동기 쿠폰 발급 메소드
     * @param command 쿠폰 발급 커맨드
     */
    @DistributedLock(key = "#command.getCouponId()", type = LockType.COUPON, strategy = LockStrategy.PUB_SUB_LOCK)
    @Transactional
    public void issueAsync(CouponIssueCommand command) {
        DomainUser user = userService.findById(command.getUserId());
        
        try {
            // Redis를 통해 쿠폰 발급 가능 여부 확인 및 수량 감소
            couponRedisService.tryIssueCoupon(command.getCouponId());
            
            // 실제 DB에 쿠폰 발급 처리
            DomainCoupon coupon = couponService.issueCoupon(command.getCouponId());
            userCouponService.issue(user.getId(), coupon.getId());
            
            log.info("쿠폰 발급 성공: 사용자 ID={}, 쿠폰 ID={}", command.getUserId(), command.getCouponId());
        } catch (Exception e) {
            // 쿠폰 발급 실패 시 Redis 수량 복구
            couponRedisService.rollbackCouponIssue(command.getCouponId());
            log.error("쿠폰 발급 실패: 사용자 ID={}, 쿠폰 ID={}, 원인={}", 
                    command.getUserId(), command.getCouponId(), e.getMessage());
            
            // 예외 재전파
            if (e instanceof ApiExceptionResponse) {
                throw e;
            }
            throw new ApiExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, "쿠폰 발급 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 쿠폰 초기화 (Redis에 쿠폰 수량 설정)
     * @param couponId 쿠폰 ID
     * @return 초기화된 쿠폰 정보
     */
    public DomainCoupon initializeCoupon(Long couponId) {
        return couponRedisService.initializeCoupon(couponId);
    }

    /**
     * 사용자의 쿠폰 목록 조회
     * @param userId 사용자 ID
     * @return 쿠폰 목록
     */
    @Transactional(readOnly = true)
    public List<CouponMeCommand> getMeCoupons(Long userId) {
        return userCouponService.getUserCoupons(userId).stream()
                .map(CouponMeCommand::toCommand).toList();
    }
}
