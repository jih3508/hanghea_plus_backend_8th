package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductStockJpaRepository extends JpaRepository<ProductStock,Long> {
    Optional<ProductStock> findByProductId(Long productId);
}
