package kr.hhplus.be.server.config.redis;


import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        // 직렬화/역직렬화 설정
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 기본 TTL 30분
                .disableCachingNullValues()  // null 값은 캐싱하지 않음
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 캐시별 TTL 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 상품 랭킹 캐시: 25시간 유효 = 1일 + 1시간
        cacheConfigurations.put("productRanks", redisCacheConfiguration().entryTtl(Duration.ofHours(25)));

        // 상위 상품 캐시: 100시간 = 3일 + 4시간
        cacheConfigurations.put("topProducts", redisCacheConfiguration().entryTtl(Duration.ofHours(100)));

        // 쿠폰 정보 캐시: 30분 유효
        cacheConfigurations.put("coupons", redisCacheConfiguration().entryTtl(Duration.ofMinutes(30)));

        // 사용자 쿠폰 목록 캐시: 10분 유효
        cacheConfigurations.put("userCoupons", redisCacheConfiguration().entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(redisCacheConfiguration())
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
