package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.domain.order.model.DomainOrderProductHistory;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("주문 서비스 통합테스트")
class OrderServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private OrderService service;

    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderProductHistoryRepository historyRepository;


    @DisplayName("주문 생성 테스트")
    void 주문_생성(){

        // given
        CreateOrder order = new CreateOrder(1L, UUID.randomUUID().toString());

        CreateOrder.OrderItem orderItem1 = CreateOrder.OrderItem.builder()
                .productId(1L)
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(100000))
                .build();

        CreateOrder.OrderItem orderItem2 = CreateOrder.OrderItem.builder()
                .productId(2L)
                .quantity(3)
                .totalPrice(BigDecimal.valueOf(100000))
                .build();

        CreateOrder.OrderItem orderItem3 = CreateOrder.OrderItem.builder()
                .productId(3L)
                .quantity(2)
                .totalPrice(BigDecimal.valueOf(100000))
                .build();

        List<CreateOrder.OrderItem> orderItems = List.of(orderItem1, orderItem2, orderItem3);
        order.setOrderItems(orderItems);
        order.setTotalPrice(BigDecimal.valueOf(300000));

        //when
        DomainOrder result = service.create(order);
        List<DomainOrderProductHistory> historyList = historyRepository.findByOrderId(result.getId());


        //then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(3);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("최근 3일 주문 데이터 가져오기")
    void 최근_주문데이터(){
        // given
        CreateOrderProductHistory create = new CreateOrderProductHistory(1L, 1L, 3);
        historyRepository.create(create);
        create = new CreateOrderProductHistory(1L, 1L, 4);
        historyRepository.create(create);

        create = new CreateOrderProductHistory(1L, 2L, 5);
        historyRepository.create(create);

        create = new CreateOrderProductHistory(1L, 2L, 6);
        historyRepository.create(create);

        create = new CreateOrderProductHistory(1L, 3L, 5);
        historyRepository.create(create);

        create = new CreateOrderProductHistory(1L, 3L, 6);
        historyRepository.create(create);

        List<OrderHistoryProductGroupVo> result = historyRepository.findGroupByProductIdThreeDays();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(3);
        

    }


}