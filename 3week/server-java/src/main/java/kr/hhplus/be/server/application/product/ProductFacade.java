package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.ProductStock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductFacade {

    private final ProductService service;

    private final ProductStockService stockService;


    /*
     * method: getProduct
     * description: 상품 조회
     */
    public ProductInfoCommand getProduct(Long id){

        Product product = service.getProduct(id);
        ProductStock stock = stockService.getStock(id);

        return ProductInfoCommand.toCommand(product, stock);
    }

}
