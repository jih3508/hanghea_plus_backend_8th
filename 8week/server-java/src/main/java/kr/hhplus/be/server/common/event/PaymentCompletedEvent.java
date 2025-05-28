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
public class PaymentCompletedEvent extends BaseEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paymentTime;

    @JsonCreator
    public PaymentCompletedEvent(
            @JsonProperty("orderId") Long orderId,
            @JsonProperty("userId") Long userId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("paymentMethod") String paymentMethod,
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("paymentTime") LocalDateTime paymentTime) {
        super("PAYMENT_COMPLETED", "ecommerce-payment-service");
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.paymentTime = paymentTime;
    }
}
