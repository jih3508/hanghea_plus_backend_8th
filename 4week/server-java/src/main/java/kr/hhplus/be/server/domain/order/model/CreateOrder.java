package kr.hhplus.be.server.domain.order.model;

import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.user.model.DomainUserCoupon;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class CreateOrder {

    private Long userId;

    private BigDecimal totalPrice;

    private String orderNumber;

    private BigDecimal discountPrice;

    List<OrderItem> orderItems;

    public CreateOrder(Long userId, String orderNumber) {
        this.userId = userId;
        this.orderNumber = orderNumber;
        this.totalPrice = BigDecimal.ZERO;
        this.discountPrice = BigDecimal.ZERO;
        this.orderItems = new LinkedList<>();
    }


    public void addOrderItem(DomainProduct product, DomainUserCoupon userCoupon, int quantity) {
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        // 상품 계산
        if(userCoupon != null) {
            BigDecimal discount = userCoupon.getDiscountPrice(totalPrice);
            this.discountPrice = totalPrice.subtract(this.discountPrice.add(discount));
            this.totalPrice = this.totalPrice.add(discount);
            totalPrice = discount;

        }else{
            this.totalPrice = totalPrice.add(totalPrice);
        }

        orderItems.add(
                OrderItem.builder()
                        .productId(product.getId())
                        .couponId(userCoupon.getCouponId())
                        .quantity(quantity)
                        .totalPrice(totalPrice)
                        .build()
        );
    }

    @Getter
    @Setter
    public static class OrderItem {

        private Long productId;
        private Long couponId;
        private Integer quantity;
        private BigDecimal totalPrice;

        @Builder
        public OrderItem(Long productId, Long couponId, Integer quantity, BigDecimal totalPrice) {
            this.productId = productId;
            this.couponId = couponId;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }
    }
}
