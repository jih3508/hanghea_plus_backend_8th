package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.infrastructure.user.entity.UserCoupon;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CouponMeCommand {

    private Long couponId;

    private String couponNumber;

    private CouponType type;

    private BigDecimal discountPrice;

    private Integer rate;

    private Boolean isUsed;

    public static CouponMeCommand toCommand(DomainUserCoupon userCoupon) {
        return CouponMeCommand.builder()
                .couponId(userCoupon.getCouponId())
                .couponNumber(userCoupon.getCouponNumber())
                .type(userCoupon.getType())
                .discountPrice(userCoupon.getDiscountPrice())
                .rate(userCoupon.getRate())
                .isUsed(userCoupon.getIsUsed())
                .build();
    }
}
