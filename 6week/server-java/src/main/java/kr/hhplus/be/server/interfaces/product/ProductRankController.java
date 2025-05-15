package kr.hhplus.be.server.interfaces.product;

import kr.hhplus.be.server.application.order.OrderFacadeRedis;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products/rank")
@RequiredArgsConstructor
public class ProductRankController {

    private final OrderFacadeRedis orderFacadeRedis;

    /**
     * 상위 5개 인기 상품 조회 (어제 날짜 기준)
     * @return 상위 5개 상품 목록
     */
    @GetMapping("/top")
    public ResponseEntity<List<DomainProductRank>> getTopProducts() {
        List<DomainProductRank> topProducts = orderFacadeRedis.getTopProducts();
        return ResponseEntity.ok(topProducts);
    }

    /**
     * 3일치 데이터 기반 상위 5개 인기 상품 조회
     * @return 3일치 데이터 기반 상위 5개 상품 목록
     */
    @GetMapping("/top/three-days")
    public ResponseEntity<List<DomainProductRank>> getTopProductsThreeDays() {
        List<DomainProductRank> topProducts = orderFacadeRedis.getTopProductsThreeDays();
        return ResponseEntity.ok(topProducts);
    }

    /**
     * 랭킹 캐시 초기화
     * @return 초기화 결과
     */
    @GetMapping("/clear-cache")
    public ResponseEntity<String> clearRankCache() {
        orderFacadeRedis.clearRankCache();
        return ResponseEntity.ok("랭킹 캐시가 초기화되었습니다.");
    }
}
