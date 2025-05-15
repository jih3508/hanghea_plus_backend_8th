package kr.hhplus.be.server.infrastructure.product.adapter;

import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import kr.hhplus.be.server.domain.product.service.ProductRankRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 기존 ProductRankRepository 인터페이스를 구현하면서 Redis 서비스를 활용하는 어댑터 클래스
 * 이를 통해 기존 코드를 최소한으로 수정하면서 새로운 Redis 기능을 활용할 수 있음
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductRankRedisAdapter implements ProductRankRepository {

    private final ProductRankRepository originalRepository;
    private final ProductRankRedisService productRankRedisService;

    @Override
    public void manySave(List<CreateProductRank> productRanks) {
        // 기존 저장소에도 저장
        originalRepository.manySave(productRanks);
    }

    @Override
    public List<DomainProductRank> todayProductRank() {
        // Redis에서 상위 상품 정보 조회
        List<DomainProductRank> redisRanks = productRankRedisService.getTopProducts();
        
        // Redis에서 데이터를 찾지 못한 경우 기존 저장소에서 조회
        if (redisRanks == null || redisRanks.isEmpty()) {
            log.info("Redis에서 상품 랭킹 정보를 찾지 못했습니다. 기존 저장소에서 조회합니다.");
            return originalRepository.todayProductRank();
        }
        
        return redisRanks;
    }
}
