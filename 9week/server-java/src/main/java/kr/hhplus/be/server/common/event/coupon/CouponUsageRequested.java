package kr.hhplus.be.server.common.event.coupon;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageRequested {
    private Long orderId;
    private Long userId;
    private Long userCouponId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    
    public static CouponUsageRequested of(Long orderId, Long userId, Long userCouponId) {
        return new CouponUsageRequested(orderId, userId, userCouponId, LocalDateTime.now());
    }
}
