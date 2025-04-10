package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.entity.ProductStock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductStockRepository {

    Optional<ProductStock> findByProductId(Long productId);

    ProductStock save(ProductStock productStock);
}
