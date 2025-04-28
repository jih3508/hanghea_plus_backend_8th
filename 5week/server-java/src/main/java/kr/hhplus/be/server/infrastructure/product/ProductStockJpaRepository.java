package kr.hhplus.be.server.infrastructure.product;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductStockJpaRepository extends JpaRepository<ProductStock,Long> {
    Optional<ProductStock> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM ProductStock ps WHERE ps.product.id = :productId")
    Optional<ProductStock> findByProductIdWithLock(@Param("productId") Long productId);

    // 직접 SQL 업데이트 추가
    @Modifying
    @Query("UPDATE ProductStock ps SET ps.quantity = ps.quantity - :quantity WHERE ps.product.id = :productId AND ps.quantity >= :quantity")
    int decrementStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
