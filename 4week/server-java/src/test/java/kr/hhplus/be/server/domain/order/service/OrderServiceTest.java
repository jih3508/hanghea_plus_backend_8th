package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.infrastructure.order.entity.Order;
import kr.hhplus.be.server.infrastructure.order.entity.OrderItem;
import kr.hhplus.be.server.infrastructure.order.entity.OrderProductHistory;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService service;

    @Mock
    private OrderRepository repository;


    @Mock
    private OrderProductHistoryRepository historyRepository;

    @Test
    @DisplayName("주문 아이템 저장 + 주문 아이템 이력 저장 테스트")
    void 아이템_이력_저장(){
        // given
        CreateOrder order = new CreateOrder(1L, UUID.randomUUID().toString());


        CreateOrder.OrderItem orderItem1 = CreateOrder.OrderItem.builder()
                .productId(1L)
                .quantity(1)
                .build();

        CreateOrder.OrderItem orderItem2 = CreateOrder.OrderItem.builder()
                .productId(2L)
                .quantity(3)
                .build();

        CreateOrder.OrderItem orderItem3 = CreateOrder.OrderItem.builder()
                .productId(3L)
                .quantity(2)
                .build();

        List<CreateOrder.OrderItem> orderItems = List.of(orderItem1, orderItem2, orderItem3);
        order.setOrderItems(orderItems);

        //when
        service.create(order);

        // then
        verify(repository, times(1)).create(order);


    }


}