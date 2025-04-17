package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    final ProductRepository repository;

    public DomainProduct getProduct(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "상품이 존재 하지 않습니다."));
    }
}
