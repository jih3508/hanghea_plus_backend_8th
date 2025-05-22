package kr.hhplus.be.server.domain.order.event;

import kr.hhplus.be.server.domain.order.model.DomainOrder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderEvent {
    private final DomainOrder order;
    private final LocalDateTime eventTime;
    private final String eventType;

    public OrderEvent(DomainOrder order, String eventType) {
        this.order = order;
        this.eventTime = LocalDateTime.now();
        this.eventType = eventType;
    }

    public static OrderEvent created(DomainOrder order) {
        return new OrderEvent(order, "ORDER_CREATED");
    }
}
