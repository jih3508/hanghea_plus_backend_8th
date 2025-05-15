package kr.hhplus.be.server.infrastructure.coupon.repository;

import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepositoryImpl implements CouponRedisRepository {

    private static final String COUPON_QUANTITY_KEY_PREFIX = "coupon:quantity:";
    
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean initializeCouponQuantity(Long couponId, int quantity) {
        String key = getCouponKey(couponId);
        // 기존 키가 없을 때만 설정 (이미 있으면 덮어쓰지 않음)
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, quantity));
    }

    @Override
    public long decrementCouponQuantity(Long couponId) {
        String key = getCouponKey(couponId);
        Long remaining = redisTemplate.opsForValue().decrement(key);
        
        // 남은 수량이 0보다 작으면 0으로 보정하고 -1 반환 (쿠폰 소진)
        if (remaining != null && remaining < 0) {
            redisTemplate.opsForValue().set(key, 0);
            return -1;
        }
        
        return remaining != null ? remaining : -1;
    }

    @Override
    public long incrementCouponQuantity(Long couponId) {
        String key = getCouponKey(couponId);
        Long newValue = redisTemplate.opsForValue().increment(key);
        return newValue != null ? newValue : 0;
    }

    @Override
    public long getCurrentCouponQuantity(Long couponId) {
        String key = getCouponKey(couponId);
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return -1;
        }
        
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        
        return -1;
    }
    
    private String getCouponKey(Long couponId) {
        return COUPON_QUANTITY_KEY_PREFIX + couponId;
    }
}
