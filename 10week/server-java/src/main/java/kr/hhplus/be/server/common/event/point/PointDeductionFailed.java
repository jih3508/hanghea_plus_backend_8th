package kr.hhplus.be.server.common.event.point;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointDeductionFailed {
    private Long orderId;
    private Long userId;
    private BigDecimal pointAmount;
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime failedAt;
    
    public static PointDeductionFailed of(Long orderId, Long userId, BigDecimal pointAmount, String reason) {
        return new PointDeductionFailed(orderId, userId, pointAmount, reason, LocalDateTime.now());
    }
}
