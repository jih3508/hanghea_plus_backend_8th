package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.util.List;
import java.util.Set;

/**
 * Redis를 사용하여 상품 랭킹 정보를 관리하는 저장소 인터페이스
 */
public interface ProductRankRedisRepository {
    
    /**
     * 특정 날짜의 상품 랭킹 정보를 Redis에 저장
     * @param date 해당 날짜 (yyyyMMdd 형식)
     * @param productId 상품 ID
     * @param score 점수 (판매량)
     */
    void addProductScore(String date, Long productId, Double score);
    
    /**
     * 특정 날짜의 상위 랭킹 상품 조회
     *
     * @param date  해당 날짜 (yyyyMMdd 형식)
     * @param count 가져올 상품 수
     * @return 상위 랭킹 상품 목록
     */
    Set<TypedTuple<Object>> getTopProducts(String date, long count);
    
    /**
     * 여러 날짜의 랭킹 정보를 합산하여 조회
     * @param dates 조회할 날짜 목록 (yyyyMMdd 형식)
     * @param count 가져올 상품 수
     * @return 합산된 상위 랭킹 상품 목록
     */
    List<DomainProductRank> getTopProductsAcrossDays(List<String> dates, long count);
}
