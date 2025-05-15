package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.model.CreateProduct;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository repository;

    @Override
    public Optional<DomainProduct> findById(Long id) {
        return repository.findById(id)
                .map(Product::toDomain);
    }

    @Override
    public DomainProduct create(CreateProduct product) {
        return repository.save(Product.create(product)).toDomain();
    }
}
