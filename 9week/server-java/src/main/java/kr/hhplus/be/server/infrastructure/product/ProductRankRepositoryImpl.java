package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import kr.hhplus.be.server.infrastructure.product.entity.DecrementRank;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductRank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ProductRankRepositoryImpl implements ProductRankRepository {

    private final ProductRankJpaRepository repository;

    private final ProductRankRedisRepository redisRepository;

    private final ProductJpaRepository productRepository;

    @Override
    public void saveAll(List<CreateProductRank> productRank) {
        List<ProductRank> productRanks = productRank.stream()
                        .map(create -> {
                            Product product = productRepository.findById(create.getProductId()).get();
                            return ProductRank.create(create, product);
                        }).toList();

        repository.saveAll(productRanks);
    }

    @Override
    public List<DomainProductRank> todayProductRank() {

        // 결과를 DomainProductRank로 변환
        int rank = 1;
        List<DomainProductRank> result = new LinkedList<>();
        Set<ZSetOperations.TypedTuple<Long>> topProducts = redisRepository.getTopProducts();

        for(ZSetOperations.TypedTuple<Long> topProduct : topProducts) {
            Product product = productRepository.findById(topProduct.getValue()).get();
            result.add(DomainProductRank.of(product, rank++,  topProduct.getScore().intValue()));
        }

        return result;
    }

    @Override
    public List<DomainProductRank> findAllProductRank() {
        return repository.findAll().stream().map(ProductRank::toDomain).toList();
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public List<DomainProductRank> findAll() {
        return repository.findAll().stream().map(ProductRank::toDomain).toList();
    }

    @Override
    public void resetRank(DecrementRank decrementRank) {
        redisRepository.decrementQuantity(decrementRank.getProductId(), decrementRank.getQuantity());
    }
}
