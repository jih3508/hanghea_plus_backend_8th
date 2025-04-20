package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.infrastructure.product.entity.ProductRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProductRankJpaRepository extends JpaRepository<ProductRank,Long> {

    @Query("SELECT pr FROM ProductRank pr WHERE pr.rankDate = CURRENT_DATE ORDER BY pr.rank ASC")
    List<ProductRank> findAllByToday(LocalDate today);
}
