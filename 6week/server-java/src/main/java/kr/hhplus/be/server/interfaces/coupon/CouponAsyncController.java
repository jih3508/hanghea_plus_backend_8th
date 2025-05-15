package kr.hhplus.be.server.interfaces.coupon;

import kr.hhplus.be.server.application.coupon.CouponFacadeRedis;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.application.coupon.CouponMeCommand;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons/async")
@RequiredArgsConstructor
public class CouponAsyncController {

    private final CouponFacadeRedis couponFacadeRedis;

    /**
     * Redis 기반 비동기 쿠폰 발급 API
     * @param userId 사용자 ID
     * @param couponId 쿠폰 ID
     * @return 발급 결과
     */
    @PostMapping("/issue")
    public ResponseEntity<String> issueCouponAsync(
            @RequestParam Long userId,
            @RequestParam Long couponId) {
        CouponIssueCommand command = new CouponIssueCommand(userId, couponId);
        couponFacadeRedis.issueAsync(command);
        return ResponseEntity.ok("쿠폰이 정상적으로 발급되었습니다.");
    }

    /**
     * 쿠폰 초기화 API (Redis에 쿠폰 수량 설정)
     * @param couponId 쿠폰 ID
     * @return 초기화된 쿠폰 정보
     */
    @PostMapping("/initialize/{couponId}")
    public ResponseEntity<DomainCoupon> initializeCoupon(@PathVariable Long couponId) {
        DomainCoupon coupon = couponFacadeRedis.initializeCoupon(couponId);
        return ResponseEntity.ok(coupon);
    }

    /**
     * 사용자의 쿠폰 목록 조회 API
     * @param userId 사용자 ID
     * @return 쿠폰 목록
     */
    @GetMapping("/me")
    public ResponseEntity<List<CouponMeCommand>> getMeCoupons(@RequestParam Long userId) {
        List<CouponMeCommand> coupons = couponFacadeRedis.getMeCoupons(userId);
        return ResponseEntity.ok(coupons);
    }
}
