package kr.hhplus.be.server.application.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CouponIssueCommand {

    private Long userId;
    private Long couponId;

    public static CouponIssueCommand of(Long userId, Long couponId) {
        return  new CouponIssueCommand(userId, couponId);
    }
}
