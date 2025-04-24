package kr.hhplus.be.server.domain.product.model;

import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class DomainProductRank {

    private Long id;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private ProductCategory category;

    private Integer rank;

    private Integer totalQuantity;
}
