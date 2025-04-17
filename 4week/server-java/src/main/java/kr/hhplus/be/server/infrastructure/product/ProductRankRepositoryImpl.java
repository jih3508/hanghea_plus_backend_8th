package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductRank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductRankRepositoryImpl implements ProductRankRepository {

    private final ProductRankJpaRepository repository;

    private final ProductJpaRepository productRepository;

    @Override
    public void manySave(List<CreateProductRank> productRank) {
        List<ProductRank> productRanks = productRank.stream()
                        .map(create -> {
                            Product product = productRepository.findById(create.getProductId()).get();
                            return ProductRank.create(create, product);
                        }).toList();

        repository.saveAll(productRanks);
    }

    @Override
    public List<DomainProductRank> todayProductRank() {
        return repository.findAllByToday(LocalDate.now())
                .stream().map(ProductRank::toDomain).toList();
    }

    @Override
    public List<DomainProductRank> findAllProductRank() {
        return repository.findAll().stream().map(ProductRank::toDomain).toList();
    }
}
