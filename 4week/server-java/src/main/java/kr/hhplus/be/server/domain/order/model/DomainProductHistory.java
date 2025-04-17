package kr.hhplus.be.server.domain.order.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class DomainProductHistory {

    private Long id;

    private Long orderId;

    private Long productId;

    private Integer quantity;

    private LocalDateTime createDateTime; // 생성 일시

    @Builder
    public DomainProductHistory(Long id, Long orderId, Long productId, Integer quantity, LocalDateTime createDateTime) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.createDateTime = createDateTime;
    }
}
