package kr.hhplus.be.server.common.event.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssued {
    private Long userId;
    private Long couponId;
    private String requestId;
    private Long userCouponId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issuedAt;
    
    public static CouponIssued of(Long userId, Long couponId, String requestId, Long userCouponId) {
        return new CouponIssued(userId, couponId, requestId, userCouponId, LocalDateTime.now());
    }
}
