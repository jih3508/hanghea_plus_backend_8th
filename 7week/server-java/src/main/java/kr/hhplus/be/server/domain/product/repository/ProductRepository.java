package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.CreateProduct;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ProductRepository {

    Optional<DomainProduct> findById(Long id);

    DomainProduct create(CreateProduct product);
}
