package kr.hhplus.be.server.common.event.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductionRequested {
    private Long orderId;
    private Long userId;
    private List<OrderItem> orderItems;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long productId;
        private int quantity;
    }
    
    public static StockDeductionRequested of(Long orderId, Long userId, List<OrderItem> orderItems) {
        return new StockDeductionRequested(orderId, userId, orderItems, LocalDateTime.now());
    }
}
