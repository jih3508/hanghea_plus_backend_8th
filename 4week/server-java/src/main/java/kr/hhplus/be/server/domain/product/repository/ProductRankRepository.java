package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.infrastructure.product.entity.ProductRank;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ProductRankRepository {

    void manySave(List<CreateProductRank> productRank);

    List<DomainProductRank> todayProductRank();

    List<DomainProductRank> findAllProductRank();

    void delleteAll();
}
