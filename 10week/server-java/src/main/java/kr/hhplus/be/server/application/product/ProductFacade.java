package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductFacade {

    private final ProductService service;

    private final ProductStockService stockService;

    private final ProductRankService rankService;


    /*
     * method: getProduct
     * description: 상품 조회
     */
    public ProductInfoCommand getProduct(Long id){

        DomainProduct product = service.getProduct(id);
        DomainProductStock stock = stockService.getStock(id);

        return ProductInfoCommand.toCommand(stock);
    }


    /*
     * method: todayProductRank
     * description: 상품 랭크 리스트
     */
    @Cacheable(value = "productRanks", key = "'today'")
    public List<DomainProductRank> todayProductRank(){

        try {
            List<DomainProductRank> result = rankService.todayProductRank();
            if (result == null || result.isEmpty()) {
                log.info("Today product rank is empty");
                return new ArrayList<>();
            }
            log.info("Today product rank loaded: {} items", result.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to load today product rank", e);
            return new ArrayList<>();  // 장애 시 빈 리스트 반환
        }

    }
}
