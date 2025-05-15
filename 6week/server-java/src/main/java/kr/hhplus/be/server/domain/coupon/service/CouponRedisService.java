package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponRedisService {

    private final CouponRepository couponRepository;
    private final CouponRedisRepository couponRedisRepository;

    /**
     * 쿠폰 정보를 Redis에 초기화
     * @param couponId 쿠폰 ID
     * @return 초기화된 쿠폰 객체
     */
    public DomainCoupon initializeCoupon(Long couponId) {
        DomainCoupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "쿠폰이 없습니다."));
        
        boolean initialized = couponRedisRepository.initializeCouponQuantity(couponId, coupon.getQuantity());
        
        if (!initialized) {
            log.info("쿠폰 ID {}는 이미 Redis에 초기화되어 있습니다.", couponId);
        }
        
        return coupon;
    }

    /**
     * 쿠폰 발급 시도 (Redis 기반)
     * @param couponId 쿠폰 ID
     * @return Redis에서 남은 쿠폰 수량
     */
    public long tryIssueCoupon(Long couponId) {
        // Redis에 쿠폰이 초기화되어 있는지 확인
        long currentQuantity = couponRedisRepository.getCurrentCouponQuantity(couponId);
        
        if (currentQuantity == -1) {
            // 초기화되어 있지 않으면 초기화
            DomainCoupon coupon = initializeCoupon(couponId);
            currentQuantity = coupon.getQuantity();
            couponRedisRepository.initializeCouponQuantity(couponId, currentQuantity);
        }
        
        // 수량이 0이면 발급 불가
        if (currentQuantity <= 0) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "발급할 쿠폰이 없습니다.");
        }
        
        // 쿠폰 수량 감소
        long remainingQuantity = couponRedisRepository.decrementCouponQuantity(couponId);
        
        if (remainingQuantity == -1) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "발급할 쿠폰이 없습니다.");
        }
        
        return remainingQuantity;
    }

    /**
     * 쿠폰 발급 롤백 (트랜잭션 실패 시)
     * @param couponId 쿠폰 ID
     * @return 롤백 후 남은 수량
     */
    public long rollbackCouponIssue(Long couponId) {
        return couponRedisRepository.incrementCouponQuantity(couponId);
    }
}
