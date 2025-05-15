package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.common.util.RedisKeysPrefix;
import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ProductRankRedisRepository {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, Long> redisTemplate;

    public void save(CreateOrderProductHistory create) {
        // 오늘, 내일, 모레 날짜 형식으로 변환
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);
        String key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + todayStr;
        redisTemplate.opsForZSet().incrementScore(key, create.getProductId(), create.getQuantity());

        String tomorrowStr = today.plusDays(1).format(DATE_FORMATTER);
        key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + tomorrowStr;
        redisTemplate.opsForZSet().incrementScore(key, create.getProductId(), create.getQuantity());

        String dayAfterTomorrowStr = today.plusDays(2).format(DATE_FORMATTER);
        key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + dayAfterTomorrowStr;
        redisTemplate.opsForZSet().incrementScore(key, create.getProductId(), create.getQuantity());

    }

    public Set<ZSetOperations.TypedTuple<Long>> getTopProducts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DATE_FORMATTER);
        String key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + dateStr;
        // Redis SortedSet은 기본적으로 오름차순이므로, 내림차순으로 가져오기 위해 reverseRangeWithScores 사용
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 5);
    }

    public void decrementQuantity(Long productId, Integer quantity) {

        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);
        String key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + todayStr;
        redisTemplate.opsForZSet().incrementScore(key, productId, -quantity);

        String tomorrowStr = today.plusDays(1).format(DATE_FORMATTER);
        key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + tomorrowStr;
        redisTemplate.opsForZSet().incrementScore(key, productId, -quantity);

        String dayAfterTomorrowStr = today.plusDays(2).format(DATE_FORMATTER);
        key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + dayAfterTomorrowStr;
        redisTemplate.opsForZSet().incrementScore(key, productId, -quantity);

    }
}
