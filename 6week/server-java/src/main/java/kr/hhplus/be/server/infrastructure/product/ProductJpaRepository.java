package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.infrastructure.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    Optional<Product> findById(Long id);
}
