package kr.hhplus.be.server.domain.product.repository;

import kr.hhplus.be.server.domain.product.entity.ProductRank;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRankRepository {

    void manySave(List<ProductRank> productRank);

    List<ProductRank> todayProductRank();
}
