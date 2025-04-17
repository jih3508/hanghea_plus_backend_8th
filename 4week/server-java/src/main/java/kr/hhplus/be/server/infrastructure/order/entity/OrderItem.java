package kr.hhplus.be.server.infrastructure.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrderItem;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @JoinColumn(name = "product_id", nullable = false)
    private Long productId;

    @JoinColumn(name = "product_id", nullable = false)
    private Long couponId;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private Integer quantity;

    @Builder
    private OrderItem(Long id, Long orderId,  Long productId, Long couponId, BigDecimal totalPrice, Integer quantity) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.couponId = couponId;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
    }

    public static OrderItem create(Long orderId, CreateOrder.OrderItem item){
        return OrderItem.builder()
                .orderId(orderId)
                .productId(item.getProductId())
                .couponId(item.getCouponId())
                .totalPrice(item.getTotalPrice())
                .quantity(item.getQuantity())
                .build();
    }

    public DomainOrderItem toDomain(){
        return DomainOrderItem.builder()
                .id(id)
                .orderId(this.orderId)
                .productId(this.productId)
                .couponId(this.couponId)
                .totalPrice(this.totalPrice)
                .quantity(this.quantity)
                .build();
    }
}
