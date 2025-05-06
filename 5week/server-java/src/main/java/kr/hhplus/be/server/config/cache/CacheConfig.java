package kr.hhplus.be.server.config.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 캐시 설정 클래스
 * Redis에 캐시를 적용하기 위한 설정
 * RedisConfig에서 이미 @EnableCaching을 사용하고 있으므로,
 * 추가적인 설정은 필요하지 않지만 문서화를 위해 클래스 추가
 */
@Configuration
public class CacheConfig {
    // 캐시 설정은 RedisConfig에서 이미 처리되고 있음
    // RedisCacheManager 설정은 RedisConfig 클래스 참조
}