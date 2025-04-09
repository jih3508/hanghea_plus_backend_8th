package kr.hhplus.be.server.coupon.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@Setter
@ToString
public class CouponIssueRequest {

    private BigInteger userId;

}
