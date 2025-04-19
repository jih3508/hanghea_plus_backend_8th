package kr.hhplus.be.server.domain.product.model;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class UpdateProductStock {

    private Long productId;

    private Integer quantity;
}
