package kr.hhplus.be.server.domain.user.model;

import kr.hhplus.be.server.domain.coupon.model.DomainCoupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.Coupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.user.entity.UserCoupon;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class DomainUserCoupon {

    private Long id;

    private Long userId;

    private Long couponId;

    private String couponNumber;

    private CouponType type;

    private Integer rate;

    private BigDecimal discountPrice;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private Boolean isUsed;

    private LocalDateTime issuedDateTime;

    public void usedCoupon() {
        this.isUsed = false;
    }

    public static DomainUserCoupon of(UserCoupon userCoupon, Coupon coupon) {
        return DomainUserCoupon.builder()
                .id(userCoupon.getId())
                .couponNumber(coupon.getCouponNumber())
                .type(coupon.getType())
                .discountPrice(coupon.getDiscountPrice())
                .startDateTime(coupon.getStartDateTime())
                .endDateTime(coupon.getEndDateTime())
                .userId(userCoupon.getUserId())
                .issuedDateTime(userCoupon.getIssuedDateTime())
                .build();
    }
}
