package kr.hhplus.be.server.common.event.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueFailed {
    private Long userId;
    private Long couponId;
    private String requestId;
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime failedAt;
    
    public static CouponIssueFailed of(Long userId, Long couponId, String requestId, String reason) {
        return new CouponIssueFailed(userId, couponId, requestId, reason, LocalDateTime.now());
    }
}
