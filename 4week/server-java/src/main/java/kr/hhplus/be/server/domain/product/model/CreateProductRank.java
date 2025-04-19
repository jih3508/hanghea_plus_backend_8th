package kr.hhplus.be.server.domain.product.model;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CreateProductRank {

    private Long productId;

    private Integer rank;

    private Integer totalQuantity;

}
