package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.common.util.RedisKeysPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository {

    private final RedisTemplate<String, Long> redisTemplate;

    public Boolean decreaseCoupon(long couponId) {
        String key = RedisKeysPrefix.COUPON_KEY_PREFIX + couponId;
        Long value = redisTemplate.opsForValue().decrement(key);

        // 체크후 0개 미만이면 발급할수 없는 쿠폰이다.
        if (value != null && value >= 0) {
            return true;
        }

        return false;
    }

    public void increaseCoupon(long couponId) {
        String key = RedisKeysPrefix.COUPON_KEY_PREFIX + couponId;
        redisTemplate.opsForValue().increment(key);
    }


}
