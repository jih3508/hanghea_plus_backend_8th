package kr.hhplus.be.server.domain.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.product.entity.Product;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProductHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Column(name = "order_id")
    private Order order;

    @ManyToOne
    @Column(name = "product_id")
    private Product product;

    private Integer quantity;

    @CreatedDate
    @Column(name = "create_date_time", updatable = false)
    private LocalDateTime createDateTime; // 생성 일시

    @Builder
    public OrderProductHistory(Long id, Order order, Product product, Integer quantity) {
        this.id = id;
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.createDateTime = LocalDateTime.now();
    }
}
