package kr.hhplus.be.server.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String couponNumber;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private Integer rate;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @CreatedDate
    @Column(name = "create_date_time", updatable = false)
    private LocalDateTime createDateTime; // 생성 일시

    @LastModifiedDate
    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime; // 수정 일시

    @Builder
    public Coupon(Long id, String couponNumber, Integer quantity, CouponType type, Integer rate, BigDecimal discountPrice, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.id = id;
        this.couponNumber = couponNumber;
        this.quantity = quantity;
        this.type = type;
        this.rate = rate;
        this.discountPrice = discountPrice;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.createDateTime = LocalDateTime.now();
    }


    public Boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        return this.startDateTime.isBefore(now) || endDateTime.isAfter(now);
    }

    public Boolean isIssued() {
        return this.quantity > 0;
    }

    public BigDecimal getDiscountPrice(BigDecimal price) {
        if(this.type.equals(CouponType.FLAT)){
            // 고정 금액 할인
            BigDecimal discounted = price.subtract(this.discountPrice);
            return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
        }else if (this.type.equals(CouponType.RATE)) {
            // 퍼센트 할인
            BigDecimal discount = price.multiply(BigDecimal.valueOf(this.rate)).divide(BigDecimal.valueOf(100));
            BigDecimal discounted = price.subtract(discount);
            return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
        }

        return price;
    }

}
