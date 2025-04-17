package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.ProductRank;
import kr.hhplus.be.server.domain.product.entity.ProductStock;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        Product product = service.getProduct(id);
        ProductStock stock = stockService.getStock(id);

        return ProductInfoCommand.toCommand(product, stock);
    }


    /*
     * method: todayProductRank
     * description: 상품 랭크 리스트
     */

    public List<ProductRankCommand> todayProductRank(){

        List<ProductRank> rank =  rankService.todayProductRank();
        List<ProductRankCommand> command = rank.stream().map(ProductRankCommand::from).toList();
        return command;

    }
}
