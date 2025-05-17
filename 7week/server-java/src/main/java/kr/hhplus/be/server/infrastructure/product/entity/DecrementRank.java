package kr.hhplus.be.server.infrastructure.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class DecrementRank {

    private Long productId;
    private Integer quantity;

}
