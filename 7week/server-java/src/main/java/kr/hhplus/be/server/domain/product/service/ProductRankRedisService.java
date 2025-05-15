package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductRankRedisService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int TOP_PRODUCTS_COUNT = 5;
    
    private final ProductRankRedisRepository productRankRedisRepository;

    /**
     * 상품 주문 정보를 Redis에 저장
     * @param productId 상품 ID
     * @param quantity 주문 수량
     */
    public void recordProductOrder(Long productId, int quantity) {
        // 오늘, 내일, 모레 날짜 형식으로 변환
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DATE_FORMATTER);
        String tomorrowStr = today.plusDays(1).format(DATE_FORMATTER);
        String dayAfterTomorrowStr = today.plusDays(2).format(DATE_FORMATTER);
        
        // 각 날짜별로 상품 주문 수량 기록
        productRankRedisRepository.addProductScore(todayStr, productId, (double) quantity);
        productRankRedisRepository.addProductScore(tomorrowStr, productId, (double) quantity);
        productRankRedisRepository.addProductScore(dayAfterTomorrowStr, productId, (double) quantity);
    }

    /**
     * 상위 5개 상품 랭킹 조회 (어제 날짜 기준)
     * @return 상위 5개 상품 목록
     */
    @Cacheable(value = "productRanks", key = "'top5'")
    public List<DomainProductRank> getTopProducts() {
        // 어제 날짜 계산
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String yesterdayStr = yesterday.format(DATE_FORMATTER);
        
        // 단일 날짜(어제)의 상위 상품 조회
        var topProducts = productRankRedisRepository.getTopProducts(yesterdayStr, TOP_PRODUCTS_COUNT);
        
        List<DomainProductRank> result = new ArrayList<>();
        int rank = 1;
        
        for (var tuple : topProducts) {
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
    }

    /**
     * 3일치 데이터로 상위 상품 조회
     * @return 3일치 데이터 기반 상위 상품 목록
     */
    @Cacheable(value = "productRanks", key = "'top5ThreeDays'")
    public List<DomainProductRank> getTopProductsThreeDays() {
        // 오늘부터 이전 3일 날짜 계산
        LocalDate today = LocalDate.now();
        List<String> dates = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(DATE_FORMATTER));
        }
        
        // 3일치 데이터 합산하여 상위 상품 조회
        return productRankRedisRepository.getTopProductsAcrossDays(dates, TOP_PRODUCTS_COUNT);
    }
}
