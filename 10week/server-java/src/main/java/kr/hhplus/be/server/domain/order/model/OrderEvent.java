package kr.hhplus.be.server.domain.order.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class OrderEvent {

    private DomainOrder order;
    private LocalDateTime eventTime;
    private String eventType;

    @JsonCreator
    public OrderEvent(
            @JsonProperty("order") DomainOrder order,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("eventTime") LocalDateTime eventTime
    ) {
        this.order = order;
        this.eventType = eventType;
        this.eventTime = eventTime != null ? eventTime : LocalDateTime.now();
    }

    public OrderEvent(DomainOrder order, String eventType) {
        this(order, eventType, LocalDateTime.now());
    }

    public static OrderEvent created(DomainOrder order) {
        return new OrderEvent(order, "ORDER_CREATED");
    }
}
