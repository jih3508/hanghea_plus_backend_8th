package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.entity.ProductStock;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final ProductStockRepository repository;

    public ProductStock getStock(Long productId) {
        return repository.findByProductId(productId)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "재고가 없습니다."));
    }


    /*
     * method: delivering
     * description: 출납처리
     */
    public ProductStock delivering(Long productId, Integer quantity){
        ProductStock stock = this.getStock(productId);
        stock.stockDelivering(quantity);
        repository.save(stock);
        return stock;
    }



}
