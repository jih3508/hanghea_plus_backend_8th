package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.common.util.RedisKeysPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepository {

    private final RedisTemplate<String, Long> redisTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String COUPON_COUNT_KEY_PREFIX = "coupon:count:";
    private static final String COUPON_ISSUED_SET_PREFIX = "coupon:issued:";

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




    /**
     * 쿠폰 발급 가능 여부 확인 및 원자적 증가
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     * @param maxCount 최대 발급 가능 수량
     * @return 발급 성공 여부
     */
    public boolean tryIssueCoupon(Long couponId, Long userId, int maxCount) {
        String countKey = COUPON_COUNT_KEY_PREFIX + couponId;
        String issuedSetKey = COUPON_ISSUED_SET_PREFIX + couponId;

        // Lua 스크립트로 원자적 연산 수행
        String luaScript = """
            local countKey = KEYS[1]
            local issuedSetKey = KEYS[2]
            local userId = ARGV[1] 
            local maxCount = tonumber(ARGV[2])
            
            -- 이미 발급받은 사용자인지 확인
            if redis.call('SISMEMBER', issuedSetKey, userId) == 1 then
                return -1  -- 이미 발급받음
            end
            
            -- 현재 발급 수량 확인
            local currentCount = tonumber(redis.call('GET', countKey)) or 0
            
            -- 최대 수량 초과 확인
            if currentCount >= maxCount then
                return 0  -- 발급 수량 초과
            end
            
            -- 발급 수량 증가 및 사용자 추가
            redis.call('INCR', countKey)
            redis.call('SADD', issuedSetKey, userId)
            
            return 1  -- 발급 성공
            """;

        try {
            RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
            Long result = redisTemplate.execute(
                    script,
                    java.util.List.of(countKey, issuedSetKey),
                    userId.toString(),
                    String.valueOf(maxCount)
            );

            if (result == null) {
                log.error("Redis 스크립트 실행 결과가 null - couponId: {}, userId: {}", couponId, userId);
                return false;
            }

            switch (result.intValue()) {
                case 1:
                    log.info("쿠폰 발급 성공 - couponId: {}, userId: {}", couponId, userId);
                    return true;
                case 0:
                    log.info("쿠폰 발급 수량 초과 - couponId: {}, userId: {}", couponId, userId);
                    return false;
                case -1:
                    log.info("이미 발급받은 쿠폰 - couponId: {}, userId: {}", couponId, userId);
                    return false;
                default:
                    log.error("알 수 없는 결과 - couponId: {}, userId: {}, result: {}", couponId, userId, result);
                    return false;
            }
        } catch (Exception e) {
            log.error("쿠폰 발급 Redis 처리 중 오류 발생 - couponId: {}, userId: {}", couponId, userId, e);
            return false;
        }
    }

    /**
     * 현재 발급 수량 조회
     */
    public int getCurrentIssueCount(Long couponId) {
        String countKey = COUPON_COUNT_KEY_PREFIX + couponId;
        String countStr = redisTemplate.opsForValue().get(countKey);
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    /**
     * 쿠폰 발급 수량 초기화 (쿠폰 생성시 호출)
     */
    public void initializeCouponCount(Long couponId) {
        String countKey = COUPON_COUNT_KEY_PREFIX + couponId;
        String issuedSetKey = COUPON_ISSUED_SET_PREFIX + couponId;

        redisTemplate.opsForValue().set(countKey, "0");
        redisTemplate.delete(issuedSetKey);

        log.info("쿠폰 발급 수량 초기화 완료 - couponId: {}", couponId);
    }

}
