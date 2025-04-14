package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.interfaces.api.order.request.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class OrderCommand {

    private Long userId;

    private List<OrderItem> items;

    @Builder
    @Getter
    public static class OrderItem{

        private Long productId;

        private Long couponId;

        private Integer quantity;
    }

    public static OrderCommand toCommand(Long userId, OrderRequest request){
        List<OrderCommand.OrderItem> list = new LinkedList<>();
        request.getItems().forEach(item->{
            OrderItem orderItem = OrderCommand.OrderItem.builder()
                    .productId(item.productId)
                    .quantity(item.quantity)
                    .build();
            list.add(orderItem);
        });

       return OrderCommand.builder()
               .userId(userId)
               .items(list)
               .build();
    }

}
