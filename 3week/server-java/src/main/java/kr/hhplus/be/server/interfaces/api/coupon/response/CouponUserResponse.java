package kr.hhplus.be.server.interfaces.api.coupon.response;

import kr.hhplus.be.server.application.coupon.CouponMeCommand;
import kr.hhplus.be.server.domain.coupon.entity.CouponType;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class CouponUserResponse {

    private Long couponId;

    private String couponNumber;

    private CouponType type;

    private BigDecimal discountPrice;

    private Integer rate;

    private Boolean isUsed;

    public static CouponUserResponse of(CouponMeCommand command) {
        return CouponUserResponse.builder()
                .couponId(command.getCouponId())
                .couponNumber(command.getCouponNumber())
                .type(command.getType())
                .discountPrice(command.getDiscountPrice())
                .rate(command.getRate())
                .isUsed(command.getIsUsed())
                .build();

    }

}
