package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductRank;
import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<ProductRankCommand> todayProductRank(){

        List<DomainProductRank> rank =  rankService.todayProductRank();
        List<ProductRankCommand> command = rank.stream().map(ProductRankCommand::from).toList();
        return command;

    }
}
