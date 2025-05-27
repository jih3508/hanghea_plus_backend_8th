package kr.hhplus.be.server.domain.order.model;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class OrderEvent {

    private DomainOrder order;
    private LocalDateTime eventTime;
    private String eventType;

    public OrderEvent(DomainOrder order, String eventType) {
        this.eventType = eventType;
        this.order = order;
        this.eventTime = LocalDateTime.now();
    }

    public static OrderEvent created(DomainOrder order) {
        return new OrderEvent(order, "ORDER_CREATED");
    }
}
