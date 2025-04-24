package kr.hhplus.be.server.domain.order.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CreateOrderProductHistory {

    private Long orderId;

    private Long productId;

    private Integer quantity;
}
