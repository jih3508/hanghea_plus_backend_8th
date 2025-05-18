package kr.hhplus.be.server.domain.user.dto;


import kr.hhplus.be.server.infrastructure.order.entity.Order;
import kr.hhplus.be.server.infrastructure.order.entity.OrderItem;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class OrderInfoDto {

    private Order order;

    private List<OrderItem> items;
}
