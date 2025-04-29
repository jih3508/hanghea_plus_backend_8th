package kr.hhplus.be.server.domain.product.model;

import kr.hhplus.be.server.infrastructure.product.entity.Product;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CreateProductStock {

    private Long productId;

    private Integer quantity;
}
