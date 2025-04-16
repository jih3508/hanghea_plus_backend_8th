package kr.hhplus.be.server.application.order;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.external.ExternalTransmissionService;
import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.domain.user.service.UserCouponService;
import kr.hhplus.be.server.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    private static final Logger log = LoggerFactory.getLogger(OrderFacadeTest.class);

    @InjectMocks
    private OrderFacade facade;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private PointService pointService;

    @Mock
    private PointHistoryService pointHistoryService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductStockService productStockService;

    @Mock
    private UserCouponService userCouponService;

    @Mock
    private ExternalTransmissionService externalTransmissionService;

    @Test
    @DisplayName("잔액이 부족할 경우 부족 테스트")
    void 주문_결제_잔액_부족(){
        // given
        OrderCommand command = mock(OrderCommand.class);
        OrderCommand.OrderItem item1 = mock(OrderCommand.OrderItem.class);
        OrderCommand.OrderItem item2 = mock(OrderCommand.OrderItem.class);
        OrderCommand.OrderItem item3 = mock(OrderCommand.OrderItem.class);

        command.setItems(List.of(item1,item2,item3));
        User user = mock(User.class);

        Order order = Order.builder().id(1L)
                .user(user)
                .totalPrice(new BigDecimal(1_000_000))
                .build();

        //when
        when(userService.findById(any())).thenReturn(user);
        when(orderService.save(any(Order.class))).thenReturn(order);
        when(pointService.use(anyLong(), any())).thenThrow(new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "잔액 부족!!!!"));


        // then
        assertThatThrownBy(() -> facade.order(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("잔액 부족!!!!");
        verify(orderService, times(1)).createOrderNumber();
        verify(orderService, times(1)).save(any(Order.class));
        verify(orderService, times(1)).save(any(Order.class), anyList());
        verify(pointService, times(1)).use(any(), any());
        verify(externalTransmissionService, never()).sendOrderData();



    }

    @Test
    @DisplayName("주문 -> 결제시 1원 이상 정상 진행 테스트")
    void 주문_결제_정상(){
        // given
        OrderCommand command = mock(OrderCommand.class);
        OrderCommand.OrderItem item1 = mock(OrderCommand.OrderItem.class);
        OrderCommand.OrderItem item2 = mock(OrderCommand.OrderItem.class);
        OrderCommand.OrderItem item3 = mock(OrderCommand.OrderItem.class);

        command.setItems(List.of(item1,item2,item3));
        User user = mock(User.class);
        Order order = Order.builder().id(1L)
                .user(user)
                .totalPrice(new BigDecimal(1_000_000))
                .build();

        //when
        when(userService.findById(any())).thenReturn(user);
        when(orderService.save(any(Order.class))).thenReturn(order);
        facade.order(command);

        // then
        verify(orderService, times(1)).createOrderNumber();
        verify(orderService, times(1)).save(any(Order.class));
        verify(orderService, times(1)).save(any(Order.class), anyList());
        verify(pointService, times(1)).use(any(), any());
        verify(pointHistoryService, times(1)).useHistory(any(), any());
        verify(externalTransmissionService, times(1)).sendOrderData();

    }

    @Test
    @DisplayName("주문 -> 결제시 0원인 경우 테스트")
    void 주문_0원_테스트(){
        // given
        OrderCommand command = mock(OrderCommand.class);
        OrderCommand.OrderItem item1 = mock(OrderCommand.OrderItem.class);
        OrderCommand.OrderItem item2 = mock(OrderCommand.OrderItem.class);
        OrderCommand.OrderItem item3 = mock(OrderCommand.OrderItem.class);

        command.setItems(List.of(item1,item2,item3));
        User user = mock(User.class);
        Order order = Order.builder().id(1L)
                .user(user)
                .totalPrice(new BigDecimal(0))
                .build();

        //when
        when(userService.findById(any())).thenReturn(user);
        when(orderService.save(any(Order.class))).thenReturn(order);
        facade.order(command);

        // then
        verify(orderService, times(1)).createOrderNumber();
        verify(orderService, times(1)).save(any(Order.class));
        verify(orderService, times(1)).save(any(Order.class), anyList());
        verify(pointService, never()).use(any(), any());
        verify(pointHistoryService, never()).useHistory(any(), any());
        verify(externalTransmissionService, times(1)).sendOrderData();

    }


}