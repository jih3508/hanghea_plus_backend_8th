package kr.hhplus.be.server.infrastructure.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainOrderProductHistory;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_product_history")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProductHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id")
    private Long productId;

    private Integer quantity;

    @CreatedDate
    @Column(name = "create_date_time", updatable = false)
    private LocalDateTime createDateTime; // 생성 일시

    @Builder
    public OrderProductHistory(Long id, Long orderId, Long productId, Integer quantity) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.createDateTime = LocalDateTime.now();
    }

    public static OrderProductHistory create(CreateOrderProductHistory create) {
        return OrderProductHistory.builder()
                .orderId(create.getOrderId())
                .productId(create.getProductId())
                .quantity(create.getQuantity())
                .build();

    }

    public DomainOrderProductHistory toDomain(){
        return DomainOrderProductHistory.builder()
                .id(this.id)
                .orderId(this.orderId)
                .productId(this.productId)
                .quantity(this.quantity)
                .createDateTime(this.createDateTime)
                .build();
    }
}
