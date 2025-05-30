package kr.hhplus.be.server.domain.coupon.service;

import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.domain.user.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponDomainService {
    
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    
    /**
     * 쿠폰 정보 조회
     */
    public DomainCoupon getCoupon(Long couponId) {
        DomainCoupon coupon = couponRepository.findById(couponId);
        if (coupon == null) {
            throw new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId);
        }
        return coupon;
    }
    
    /**
     * 사용자에게 쿠폰 발급
     */
    @Transactional
    public DomainUserCoupon issueCouponToUser(Long userId, Long couponId) {
        DomainCoupon coupon = getCoupon(couponId);
        
        // 사용자 쿠폰 생성 및 저장
        DomainUserCoupon userCoupon = DomainUserCoupon.create(userId, couponId, coupon.getExpiredAt());
        userCouponRepository.save(userCoupon);
        
        log.info("쿠폰 발급 완료 - userId: {}, couponId: {}, userCouponId: {}", 
                userId, couponId, userCoupon.getId());
        
        return userCoupon;
    }
    
    /**
     * 쿠폰 사용 처리
     */
    @Transactional
    public boolean useCoupon(Long userCouponId, Long orderId) {
        try {
            DomainUserCoupon userCoupon = userCouponRepository.findById(userCouponId);
            if (userCoupon == null) {
                log.warn("사용자 쿠폰을 찾을 수 없음 - userCouponId: {}", userCouponId);
                return false;
            }
            
            if (userCoupon.isUsed()) {
                log.warn("이미 사용된 쿠폰 - userCouponId: {}", userCouponId);
                return false;
            }
            
            if (userCoupon.isExpired()) {
                log.warn("만료된 쿠폰 - userCouponId: {}", userCouponId);
                return false;
            }
            
            // 쿠폰 사용 처리
            userCoupon.use(orderId);
            userCouponRepository.save(userCoupon);
            
            log.info("쿠폰 사용 완료 - userCouponId: {}, orderId: {}", userCouponId, orderId);
            return true;
            
        } catch (Exception e) {
            log.error("쿠폰 사용 중 오류 발생 - userCouponId: {}, orderId: {}", userCouponId, orderId, e);
            return false;
        }
    }
    
    /**
     * 주문 취소시 쿠폰 복원
     */
    @Transactional
    public boolean restoreCouponForOrder(Long orderId) {
        try {
            // 주문에 사용된 쿠폰 조회
            DomainUserCoupon userCoupon = userCouponRepository.findByOrderId(orderId);
            if (userCoupon == null) {
                log.info("주문에 사용된 쿠폰이 없음 - orderId: {}", orderId);
                return true;
            }
            
            // 쿠폰 복원 (사용 취소)
            userCoupon.restore();
            userCouponRepository.save(userCoupon);
            
            log.info("쿠폰 복원 완료 - orderId: {}, userCouponId: {}", orderId, userCoupon.getId());
            return true;
            
        } catch (Exception e) {
            log.error("쿠폰 복원 중 오류 발생 - orderId: {}", orderId, e);
            return false;
        }
    }
}
