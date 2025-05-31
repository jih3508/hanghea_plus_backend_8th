package kr.hhplus.be.server.common.event.coupon.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssueRequested {
    private Long userId;
    private Long couponId;
    private String requestId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    
    public static CouponIssueRequested of(Long userId, Long couponId, String requestId) {
        return new CouponIssueRequested(userId, couponId, requestId, LocalDateTime.now());
    }
}
