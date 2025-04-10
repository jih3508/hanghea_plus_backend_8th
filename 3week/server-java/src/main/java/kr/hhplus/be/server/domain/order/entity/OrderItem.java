package kr.hhplus.be.server.domain.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.product.entity.Product;
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


    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Coupon coupon;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    private Integer quantity;

    @Builder
    public OrderItem(Long id, Order order, Product product, Coupon coupon, BigDecimal totalPrice, Integer quantity) {
        this.id = id;
        this.order = order;
        this.product = product;
        this.coupon = coupon;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
    }
}
