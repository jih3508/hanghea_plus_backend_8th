package kr.hhplus.be.server.common.event.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageFailed {
    private Long orderId;
    private Long userId;
    private Long userCouponId;
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime failedAt;
    
    public static CouponUsageFailed of(Long orderId, Long userId, Long userCouponId, String reason) {
        return new CouponUsageFailed(orderId, userId, userCouponId, reason, LocalDateTime.now());
    }
}
