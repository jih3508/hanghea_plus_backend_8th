package kr.hhplus.be.server.domain.coupon.model;

import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CreateCoupon {

    private String couponNumber;

    private Integer quantity;

    private CouponType type;

    private Integer rate;

    private BigDecimal discountPrice;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;
}
