package kr.hhplus.be.server.domain.order.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Column(name = "user_id")
    private User user;

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
    public Order(Long id, User user, String orderNumber, BigDecimal totalPrice, BigDecimal discountPrice) {
        this.id = id;
        this.user = user;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.discountPrice = discountPrice;
    }
}
