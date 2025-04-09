package kr.hhplus.be.server.order.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderRequest {

    private BigInteger userId;

    private List<OrderItem> items;

    @Getter
    @Setter
    @ToString
    private static class OrderItem{
        private BigInteger productId;
        private Integer quantity;
    }
}
