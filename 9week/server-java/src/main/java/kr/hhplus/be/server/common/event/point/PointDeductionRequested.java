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
public class PointDeductionRequested {
    private Long orderId;
    private Long userId;
    private BigDecimal pointAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    
    public static PointDeductionRequested of(Long orderId, Long userId, BigDecimal pointAmount) {
        return new PointDeductionRequested(orderId, userId, pointAmount, LocalDateTime.now());
    }
}
