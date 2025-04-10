package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.entity.Order;
import kr.hhplus.be.server.domain.order.entity.OrderItem;
import kr.hhplus.be.server.domain.order.entity.OrderProductHistory;
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService service;

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderItemRepository itemRepository;

    @Mock
    private OrderProductHistoryRepository historyRepository;

    @Test
    @DisplayName("주문 아이템 저장 + 주문 아이템 이력 저장 테스트")
    void 아이템_이력_저장(){
        // given
        Order order = Order.builder().id(1L).build();

        Product product1 = Product.builder().id(1L).build();
        Product product2 = Product.builder().id(1L).build();
        Product product3 = Product.builder().id(2L).build();

        OrderItem orderItem1 = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product1)
                .quantity(1)
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product2)
                .quantity(3)
                .build();

        OrderItem orderItem3 = OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product3)
                .quantity(2)
                .build();

        List<OrderItem> orderItems = List.of(orderItem1, orderItem2, orderItem3);


        //when
        service.save(order, orderItems);

        // then
        verify(itemRepository, times(1)).manySave(orderItems);
        verify(historyRepository, times(3)).save(any(OrderProductHistory.class));

    }


}