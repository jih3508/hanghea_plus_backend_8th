package kr.hhplus.be.server.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreatedEvent extends BaseEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private List<OrderItem> orderItems;
    private String orderStatus;

    @JsonCreator
    public OrderCreatedEvent(
            @JsonProperty("orderId") Long orderId,
            @JsonProperty("userId") Long userId,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("orderItems") List<OrderItem> orderItems,
            @JsonProperty("orderStatus") String orderStatus) {
        super("ORDER_CREATED", "ecommerce-order-service");
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderItems = orderItems;
        this.orderStatus = orderStatus;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItem {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;

        @JsonCreator
        public OrderItem(
                @JsonProperty("productId") Long productId,
                @JsonProperty("productName") String productName,
                @JsonProperty("quantity") Integer quantity,
                @JsonProperty("price") BigDecimal price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
