package kr.hhplus.be.server.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 캐시에서 데이터를 가져오거나, 캐시에 없는 경우 데이터를 생성하고 캐시에 저장
     *
     * @param key 캐시 키
     * @param ttl 캐시 유효 시간 (초)
     * @param supplier 데이터 생성 함수
     * @param <T> 반환 데이터 타입
     * @return 캐시된 데이터 또는 새로 생성된 데이터
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(String key, long ttl, Supplier<T> supplier) {
        Object cached = redisTemplate.opsForValue().get(key);
        
        if (cached != null) {
            log.debug("Cache hit: {}", key);
            return (T) cached;
        }
        
        log.debug("Cache miss: {}", key);
        T value = supplier.get();
        
        if (value != null) {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttl));
            log.debug("Cached: {} for {} seconds", key, ttl);
        }
        
        return value;
    }
    
    /**
     * 기본 10분 TTL로 캐시 처리
     */
    public <T> T getOrCreate(String key, Supplier<T> supplier) {
        return getOrCreate(key, 600, supplier);
    }
    
    /**
     * 캐시 삭제
     */
    public void invalidate(String key) {
        redisTemplate.delete(key);
        log.debug("Cache invalidated: {}", key);
    }
    
    /**
     * 패턴에 일치하는 모든 캐시 삭제
     */
    public void invalidatePattern(String pattern) {
        redisTemplate.keys(pattern).forEach(this::invalidate);
    }
    
    /**
     * 캐시의 만료 시간 설정
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }
    
    /**
     * 캐시 존재 여부 확인
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
