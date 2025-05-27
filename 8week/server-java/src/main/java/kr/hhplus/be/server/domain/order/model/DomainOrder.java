package kr.hhplus.be.server.domain.order.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@ToString
public class DomainOrder {

    private Long id;

    private Long userId;

    private String orderNumber;

    private BigDecimal totalPrice;

    private BigDecimal discountPrice;

    private List<DomainOrderItem>  items = new ArrayList<>();

    @Builder
    public DomainOrder(Long id, Long userId, String orderNumber, BigDecimal totalPrice, BigDecimal discountPrice) {
        this.id = id;
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.discountPrice = discountPrice;
        this.items = new LinkedList<>();
    }

    public void addItem(DomainOrderItem item) {
        this.items.add(item);
    }

    public void addItems(List<DomainOrderItem> items) {
        this.items.addAll(items);
    }
}
