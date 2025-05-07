package kr.hhplus.be.server.infrastructure.coupon.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.model.CreateCoupon;
import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
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
    @Column(name = "created_date_time", updatable = false)
    private LocalDateTime createdDateTime; // 생성 일시

    @LastModifiedDate
    @Column(name = "updated_date_time")
    private LocalDateTime updatedDateTime; // 수정 일시

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
        this.createdDateTime = LocalDateTime.now();
    }


    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public static Coupon createCoupon(CreateCoupon createCoupon) {
        return Coupon.builder()
                .couponNumber(createCoupon.getCouponNumber())
                .quantity(createCoupon.getQuantity())
                .type(createCoupon.getType())
                .rate(createCoupon.getRate())
                .discountPrice(createCoupon.getDiscountPrice())
                .startDateTime(createCoupon.getStartDateTime())
                .endDateTime(createCoupon.getEndDateTime())
                .build();
    }

    public DomainCoupon toDomain() {
        return DomainCoupon.builder()
                .id(this.id)
                .couponNumber(this.couponNumber)
                .quantity(this.quantity)
                .type(this.type)
                .rate(this.rate)
                .discountPrice(this.discountPrice)
                .startDateTime(this.startDateTime)
                .endDateTime(this.endDateTime)
                .build();
    }



}
