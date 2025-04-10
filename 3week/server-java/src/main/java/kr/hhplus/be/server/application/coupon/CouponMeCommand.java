package kr.hhplus.be.server.application.coupon;

import kr.hhplus.be.server.domain.coupon.entity.CouponType;
import kr.hhplus.be.server.domain.user.entity.UserCoupon;
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

    public static CouponMeCommand toCommand(UserCoupon userCoupon) {
        return CouponMeCommand.builder()
                .couponId(userCoupon.getCoupon().getId())
                .couponNumber(userCoupon.getCoupon().getCouponNumber())
                .type(userCoupon.getCoupon().getType())
                .discountPrice(userCoupon.getCoupon().getDiscountPrice())
                .rate(userCoupon.getCoupon().getRate())
                .isUsed(userCoupon.getIsUsed())
                .build();
    }
}
