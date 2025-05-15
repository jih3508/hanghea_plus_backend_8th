package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.infrastructure.product.entity.DecrementRank;

import java.util.List;

public interface ProductRankRepository {

    void manySave(List<CreateProductRank> productRank);

    List<DomainProductRank> todayProductRank();

    List<DomainProductRank> findAllProductRank();
        
    void deleteAll();

    List<DomainProductRank> findAll();

    void resetRank(DecrementRank decrementRank);
}
