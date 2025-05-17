package kr.hhplus.be.server.domain.product.model;

import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CreateProduct {

    private String name;

    private BigDecimal price;

    private String productNumber;

    private ProductCategory category;
}
