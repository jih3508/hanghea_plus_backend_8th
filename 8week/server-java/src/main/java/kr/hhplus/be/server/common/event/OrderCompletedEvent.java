package kr.hhplus.be.server.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderCompletedEvent extends BaseEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private LocalDateTime completedAt;
    private String paymentMethod;

    @JsonCreator
    public OrderCompletedEvent(
            @JsonProperty("orderId") Long orderId,
            @JsonProperty("userId") Long userId,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("completedAt") LocalDateTime completedAt,
            @JsonProperty("paymentMethod") String paymentMethod) {
        super("ORDER_COMPLETED", "ecommerce-order-service");
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.completedAt = completedAt;
        this.paymentMethod = paymentMethod;
    }
}
