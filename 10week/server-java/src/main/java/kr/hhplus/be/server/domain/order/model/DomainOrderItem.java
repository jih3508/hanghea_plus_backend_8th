package kr.hhplus.be.server.domain.order.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class DomainOrderItem {

    private Long id;

    private Long orderId;

    private Long productId;

    private Long couponId;

    private BigDecimal totalPrice;

    private Integer quantity;
}
