package kr.hhplus.be.server.infrastructure.product.repository;

import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRankRedisRepositoryImpl implements ProductRankRedisRepository {

    private static final String PRODUCT_RANK_KEY_PREFIX = "product:rank:";
    
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addProductScore(String date, Long productId, Double score) {
        String key = PRODUCT_RANK_KEY_PREFIX + date;
        redisTemplate.opsForZSet().incrementScore(key, productId.toString(), score);
    }

    @Override
    public Set<TypedTuple<String>> getTopProducts(String date, long count) {
        String key = PRODUCT_RANK_KEY_PREFIX + date;
        // Redis SortedSet은 기본적으로 오름차순이므로, 내림차순으로 가져오기 위해 reverseRangeWithScores 사용
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, count - 1);
    }

    @Override
    public List<DomainProductRank> getTopProductsAcrossDays(List<String> dates, long count) {
        // 여러 날짜의 키 생성
        String[] keys = dates.stream()
                .map(date -> PRODUCT_RANK_KEY_PREFIX + date)
                .toArray(String[]::new);
        
        // 임시 저장소 키
        String destinationKey = "temp:product:rank:" + UUID.randomUUID();
        
        try {
            // 여러 날짜의 데이터를 합산하여 임시 키에 저장
            redisTemplate.opsForZSet().unionAndStore(keys[0], Arrays.copyOfRange(keys, 1, keys.length), destinationKey);
            
            // 상위 랭킹 조회
            Set<TypedTuple<String>> topProducts = redisTemplate.opsForZSet().reverseRangeWithScores(destinationKey, 0, count - 1);
            
            if (topProducts == null || topProducts.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 결과를 DomainProductRank로 변환
            int rank = 1;
            List<DomainProductRank> result = new ArrayList<>();
            
            for (TypedTuple<String> tuple : topProducts) {
                if (tuple.getValue() == null || tuple.getScore() == null) {
                    continue;
                }
                
                Long productId = Long.parseLong(tuple.getValue());
                int totalQuantity = tuple.getScore().intValue();
                
                DomainProductRank productRank = DomainProductRank.builder()
                        .productId(productId)
                        .totalQuantity(totalQuantity)
                        .rank(rank++)
                        .build();
                
                result.add(productRank);
            }
            
            return result;
        } finally {
            // 임시 키 삭제
            redisTemplate.delete(destinationKey);
        }
    }
}
