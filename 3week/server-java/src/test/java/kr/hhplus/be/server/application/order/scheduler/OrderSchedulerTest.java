package kr.hhplus.be.server.application.order.scheduler;

import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSchedulerTest {

    private static final Logger log = LoggerFactory.getLogger(OrderSchedulerTest.class);

    @InjectMocks
    private OrderScheduler scheduler;

    @Mock
    private  OrderService orderService;

    @Mock
    private  ProductService productService;

    @Mock
    private  ProductRankService productRankService;


    @Test
    @DisplayName("랭킹이력 저장 테스트")
    void saveRank(){
        // given
        Product product = mock(Product.class);

        OrderHistoryProductGroupVo groupVo1 = mock(OrderHistoryProductGroupVo.class);
        OrderHistoryProductGroupVo groupVo2 = mock(OrderHistoryProductGroupVo.class);
        OrderHistoryProductGroupVo groupVo3 = mock(OrderHistoryProductGroupVo.class);
        OrderHistoryProductGroupVo groupVo4 = mock(OrderHistoryProductGroupVo.class);
        List<OrderHistoryProductGroupVo> list = List.of(groupVo1, groupVo2, groupVo3, groupVo4);


        // when
        when(productService.getProduct(anyLong())).thenReturn(product);

        //then
        verify(productService, times(4)).getProduct(anyLong());
        verify(productRankService, times(1)).save(anyList());

    }

}