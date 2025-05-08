package kr.hhplus.be.server.config.redis;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);


        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(1L))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer()));

        return RedisCacheManager
                .RedisCacheManagerBuilder
                .fromConnectionFactory(factory)
                .cacheDefaults(cacheConfig)
                .build();
    }

    public Jackson2JsonRedisSerializer<Object>  jackson2JsonRedisSerializer(){
        // Jackson2JsonRedisSerializer 설정
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // ObjectMapper 설정
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        mapper.registerModule(new JavaTimeModule()); // 날짜 처리를 위한 모듈 (필요한 경우)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        serializer.serialize(mapper);
        return serializer;
    }
}
