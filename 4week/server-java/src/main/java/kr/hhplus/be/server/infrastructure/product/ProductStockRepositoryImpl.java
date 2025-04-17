package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.model.CreateProductStock;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.domain.product.model.UpdateProductStock;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductStockRepositoryImpl implements ProductStockRepository {

    private final ProductStockJpaRepository repository;

    private final ProductJpaRepository productRepository;

    @Override
    public Optional<DomainProductStock> findByProductId(Long productId) {
        return repository.findByProductId(productId)
                .map(ProductStock::toDomain);
    }

    @Override
    public DomainProductStock create(CreateProductStock stock) {
        Product product = productRepository.findById(stock.getProductId()).get();

        return repository.save(ProductStock.create(stock, product)).toDomain();
    }

    @Override
    public DomainProductStock update(UpdateProductStock productStock) {

        ProductStock stock = repository.findByProductId(productStock.getProductId()).get();
        stock.setQuantity(productStock.getQuantity());

        return repository.save(stock).toDomain();
    }
}
