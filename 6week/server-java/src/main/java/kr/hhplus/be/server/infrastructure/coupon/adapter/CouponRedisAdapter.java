package kr.hhplus.be.server.infrastructure.coupon.adapter;

import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.model.UpdateCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.coupon.service.CouponRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 기존 CouponRepository 인터페이스를 구현하면서 Redis 서비스를 활용하는 어댑터 클래스
 * 이를 통해 기존 코드를 최소한으로 수정하면서 새로운 Redis 기능을 활용할 수 있음
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CouponRedisAdapter implements CouponRepository {

    private final CouponRepository originalRepository;
    private final CouponRedisService couponRedisService;

    @Override
    public Optional<DomainCoupon> findById(Long id) {
        return originalRepository.findById(id);
    }

    @Override
    public DomainCoupon update(UpdateCoupon updateCoupon) {
        // Redis에서도 쿠폰 수량을 업데이트 (Redis에 키가 없는 경우 생성)
        try {
            // 기존 쿠폰 정보 가져오기
            DomainCoupon coupon = originalRepository.findById(updateCoupon.getId())
                    .orElseThrow(() -> new RuntimeException("쿠폰이 존재하지 않습니다: " + updateCoupon.getId()));
            
            // Redis에 초기화 (이미 있다면 무시됨)
            couponRedisService.initializeCoupon(updateCoupon.getId());
            
            // 기존 저장소에도 업데이트
            return originalRepository.update(updateCoupon);
        } catch (Exception e) {
            log.error("쿠폰 업데이트 중 오류 발생: {}", e.getMessage(), e);
            // 실패 시 기존 방식으로 진행
            return originalRepository.update(updateCoupon);
        }
    }
}
