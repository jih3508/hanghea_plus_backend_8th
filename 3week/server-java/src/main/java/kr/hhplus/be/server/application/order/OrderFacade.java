package kr.hhplus.be.server.application.order;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.coupon.entity.Coupon;
import kr.hhplus.be.server.domain.external.ExternalTransmissionService;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {

    private final OrderService service;

    private final UserService userService;

    private final PointService  pointService;

    private final PointHistoryService pointHistoryService;

    private final ProductService productService;

    private final ProductStockService productStockService;

    private final UserCouponService userCouponService;

    private final OrderService orderService;

    private final ExternalTransmissionService  externalTransmissionService;


    @Transactional
    public void order(OrderCommand command){

        User user = userService.findById(command.getUserId());

        // 주문 처리
        List<OrderItem> items = new LinkedList<>();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for(OrderCommand.OrderItem item : command.getItems()){
            OrderItem orderItem = productProcess(command.getUserId(), item);
            totalPrice.add(orderItem.getTotalPrice());
            items.add(orderItem);
        }

        Order order = Order.builder()
                .user(user)
                .orderNumber(service.createOrderNumber())
                .totalPrice(totalPrice)
                .discountPrice(totalPrice)
                .build();

        order = orderService.save(order);
        orderService.save(order, items);

        // 결제 처리
        if(order.getTotalPrice().compareTo(totalPrice) > 0){
            pointService.use(command.getUserId(), totalPrice);
            pointHistoryService.useHistory(user, totalPrice);
        }

        // 외부 데이터 전송
        externalTransmissionService.sendOrderData();
    }


    /*
     * method: productProcess
     * description: 상품 처리및 계산
     */
    private OrderItem productProcess(Long userId , OrderCommand.OrderItem item){
        Product product = productService.getProduct(item.getProductId());
        productStockService.delivering(item.getProductId(), item.getQuantity());
        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
        // 쿠폰처리
        Coupon coupon = null;
        if(item.getCouponId() != null){
            coupon = userCouponService.getUseCoupon(userId, item.getCouponId());
            if(coupon.isExpired()){
                throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "쿠폰 기간 만료 되었습니다.");
            }
            totalPrice = coupon.getDiscountPrice(totalPrice);
        }


        return OrderItem.builder()
                .product(product)
                .quantity(item.getQuantity())
                .totalPrice(totalPrice)
                .coupon(coupon)
                .build();
    }


}
