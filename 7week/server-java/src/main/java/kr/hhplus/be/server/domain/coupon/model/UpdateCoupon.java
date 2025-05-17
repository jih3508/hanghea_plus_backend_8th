package kr.hhplus.be.server.domain.coupon.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class UpdateCoupon {

    private Long couponId;

    private Integer quantity;

}
