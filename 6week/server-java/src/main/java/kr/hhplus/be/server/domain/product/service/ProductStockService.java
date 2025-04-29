package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.domain.product.model.UpdateProductStock;
import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final ProductStockRepository repository;

    public DomainProductStock getStock(Long productId) {
        return repository.findByProductId(productId)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "재고가 정보가 없습니다."));
    }


    /*
     * method: delivering
     * description: 출납처리
     */
    public DomainProductStock delivering(Long productId, Integer quantity){
        DomainProductStock stock = this.getStock(productId);
        stock.stockDelivering(quantity);
        return repository.update(UpdateProductStock.builder()
                .productId(productId)
                .quantity(stock.getQuantity())
                .build());
    }



}
