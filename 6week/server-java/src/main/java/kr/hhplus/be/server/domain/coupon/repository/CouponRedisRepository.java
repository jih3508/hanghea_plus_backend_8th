package kr.hhplus.be.server.domain.coupon.repository;

/**
 * Redis를 사용하여 쿠폰 발급 정보를 관리하는 저장소 인터페이스
 */
public interface CouponRedisRepository {
    
    /**
     * 쿠폰 수량 초기화 (Redis에 저장)
     * @param couponId 쿠폰 ID
     * @param quantity 초기 수량
     * @return 저장 성공 여부
     */
    boolean initializeCouponQuantity(Long couponId, int quantity);
    
    /**
     * 쿠폰 수량 감소 (쿠폰 발급 시 호출)
     * @param couponId 쿠폰 ID
     * @return 감소 후 남은 수량, 실패 시 -1 반환
     */
    long decrementCouponQuantity(Long couponId);
    
    /**
     * 쿠폰 수량 증가 (트랜잭션 실패 시 롤백 용도)
     * @param couponId 쿠폰 ID
     * @return 증가 후 수량
     */
    long incrementCouponQuantity(Long couponId);
    
    /**
     * 현재 쿠폰 남은 수량 조회
     * @param couponId 쿠폰 ID
     * @return 남은 수량, 키가 없으면 -1 반환
     */
    long getCurrentCouponQuantity(Long couponId);
}
