package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.*;
import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ProductStockRepository {

    Optional<DomainProductStock> findByProductId(Long productId);

    DomainProductStock create(CreateProductStock product);

    DomainProductStock update(UpdateProductStock productStock);
}
