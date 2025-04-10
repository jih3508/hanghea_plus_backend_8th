package kr.hhplus.be.server.interfaces.api.coupon.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@Setter
@ToString
public class CouponUserResponse {

    private BigInteger couponId;

    private String couponNumber;

    private String type;

    private Integer rate;

    private Integer discountPrice;
}
