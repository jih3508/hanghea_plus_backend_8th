package kr.hhplus.be.server.infrastructure.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_numbe")
    private String orderNumber;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "discount_price")
    private BigDecimal discountPrice;

    @CreatedDate
    @Column(name = "create_date_time", updatable = false)
    private LocalDateTime createDateTime; // 생성 일시

    @LastModifiedDate
    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime; // 수정 일시

    @Builder
    public Order(Long id, Long userId, String orderNumber, BigDecimal totalPrice, BigDecimal discountPrice) {
        this.id = id;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.discountPrice = discountPrice;
    }

    public static Order create(CreateOrder createOrder) {
        return Order.builder()
                .userId(createOrder.getUserId())
                .orderNumber(createOrder.getOrderNumber())
                .totalPrice(createOrder.getTotalPrice())
                .discountPrice(createOrder.getDiscountPrice())
                .build();
    }

    public DomainOrder toDomain() {
        return DomainOrder.builder()
                .id(this.id)
                .orderNumber(this.orderNumber)
                .totalPrice(this.totalPrice)
                .discountPrice(this.discountPrice)
                .build();
    }
}
