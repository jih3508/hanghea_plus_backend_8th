package kr.hhplus.be.server.interfaces.api.order;

import kr.hhplus.be.server.interfaces.api.order.request.OrderRequest;
import kr.hhplus.be.server.support.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class OrderControllerE2ETest extends E2ETest {

    @Test
    @DisplayName("주문 생성 성공 E2E 테스트")
    void createOrderSuccessE2ETest() throws Exception {
        // given
        Long userId = 1L; // 사용자 1 (포인트 10000 보유)
        
        OrderRequest request = new OrderRequest();
        List<OrderRequest.OrderItem> items = new ArrayList<>();
        
        OrderRequest.OrderItem item1 = new OrderRequest.OrderItem();
        item1.setProductId(1L); // Test Product 1 (가격: 1000)
        item1.setQuantity(2);   // 총 2000원
        
        OrderRequest.OrderItem item2 = new OrderRequest.OrderItem();
        item2.setProductId(2L); // Test Product 2 (가격: 2000)
        item2.setQuantity(1);   // 총 2000원
        
        items.add(item1);
        items.add(item2);
        request.setItems(items);
        
        // when & then
        mockMvc.perform(post("/api/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }
    
    @Test
    @DisplayName("주문 생성 - 쿠폰 적용 E2E 테스트")
    void createOrderWithCouponE2ETest() throws Exception {
        // given
        Long userId = 1L; // 사용자 1 (쿠폰 1번 보유)
        
        OrderRequest request = new OrderRequest();
        List<OrderRequest.OrderItem> items = new ArrayList<>();
        
        OrderRequest.OrderItem item = new OrderRequest.OrderItem();
        item.setProductId(1L);  // Test Product 1 (가격: 1000)
        item.setCouponId(1L);   // FLAT500 쿠폰 적용 (할인: 500)
        item.setQuantity(1);    // 총 500원 (1000 - 500)
        
        items.add(item);
        request.setItems(items);
        
        // when & then
        mockMvc.perform(post("/api/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }
    
    @Test
    @DisplayName("주문 생성 - 포인트 부족 실패 E2E 테스트")
    void createOrderInsufficientPointsE2ETest() throws Exception {
        // given
        Long userId = 2L; // 사용자 2 (포인트 5000 보유)
        
        OrderRequest request = new OrderRequest();
        List<OrderRequest.OrderItem> items = new ArrayList<>();
        
        OrderRequest.OrderItem item = new OrderRequest.OrderItem();
        item.setProductId(3L);  // Test Product 3 (가격: 3000)
        item.setQuantity(2);    // 총 6000원 (포인트 부족)
        
        items.add(item);
        request.setItems(items);
        
        // when & then
        mockMvc.perform(post("/api/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("주문 생성 - 상품 재고 부족 실패 E2E 테스트")
    void createOrderInsufficientStockE2ETest() throws Exception {
        // given
        Long userId = 1L; // 사용자 1 (포인트 10000 보유)
        
        OrderRequest request = new OrderRequest();
        List<OrderRequest.OrderItem> items = new ArrayList<>();
        
        OrderRequest.OrderItem item = new OrderRequest.OrderItem();
        item.setProductId(2L);  // Test Product 2 (재고: 5)
        item.setQuantity(10);   // 재고 부족
        
        items.add(item);
        request.setItems(items);
        
        // when & then
        mockMvc.perform(post("/api/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자의 주문 생성 실패 E2E 테스트")
    void createOrderNonExistingUserE2ETest() throws Exception {
        // given
        Long nonExistingUserId = 999L;
        
        OrderRequest request = new OrderRequest();
        List<OrderRequest.OrderItem> items = new ArrayList<>();
        
        OrderRequest.OrderItem item = new OrderRequest.OrderItem();
        item.setProductId(1L);
        item.setQuantity(1);
        
        items.add(item);
        request.setItems(items);
        
        // when & then
        mockMvc.perform(post("/api/orders/{userId}", nonExistingUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("존재하지 않는 상품으로 주문 생성 실패 E2E 테스트")
    void createOrderNonExistingProductE2ETest() throws Exception {
        // given
        Long userId = 1L;
        
        OrderRequest request = new OrderRequest();
        List<OrderRequest.OrderItem> items = new ArrayList<>();
        
        OrderRequest.OrderItem item = new OrderRequest.OrderItem();
        item.setProductId(999L); // 존재하지 않는 상품
        item.setQuantity(1);
        
        items.add(item);
        request.setItems(items);
        
        // when & then
        mockMvc.perform(post("/api/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
