package kr.hhplus.be.server.common.event.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionCompleted {
    private Long orderId;
    private Long userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    public static StockDeductionCompleted of(Long orderId, Long userId) {
        return new StockDeductionCompleted(orderId, userId, LocalDateTime.now());
    }
}
