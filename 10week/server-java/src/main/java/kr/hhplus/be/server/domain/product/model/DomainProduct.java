package kr.hhplus.be.server.domain.product.model;

import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class DomainProduct {

    private Long id;

    private String name;

    private BigDecimal price;

    private ProductCategory category;
}
