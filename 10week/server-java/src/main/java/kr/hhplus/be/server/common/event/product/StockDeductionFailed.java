package kr.hhplus.be.server.common.event.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionFailed {
    private Long orderId;
    private Long userId;
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime failedAt;
    
    public static StockDeductionFailed of(Long orderId, Long userId, String reason) {
        return new StockDeductionFailed(orderId, userId, reason, LocalDateTime.now());
    }
}
