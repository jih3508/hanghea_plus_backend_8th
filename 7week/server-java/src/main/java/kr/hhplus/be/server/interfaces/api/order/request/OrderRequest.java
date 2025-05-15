package kr.hhplus.be.server.interfaces.api.order.request;

import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.application.order.OrderCommand;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderRequest {

    private List<OrderRequest.OrderItem> items = new ArrayList<>();

    @Getter
    @Setter
    public static class OrderItem{

        @NotNull
        private Long productId;

        private Long couponId;

        @NotNull
        private Integer quantity;
    }
}
