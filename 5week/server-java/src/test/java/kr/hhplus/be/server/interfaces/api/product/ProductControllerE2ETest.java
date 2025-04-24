package kr.hhplus.be.server.interfaces.api.product;

import kr.hhplus.be.server.support.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ProductControllerE2ETest extends E2ETest {

    @Test
    @DisplayName("상품 정보 조회 E2E 테스트")
    void getProductE2ETest() throws Exception {
        // given
        Long productId = 1L;

        // when & then
        mockMvc.perform(get("/api/products/{productId}", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Product 1"))
                .andExpect(jsonPath("$.data.price").value(1000))
                .andExpect(jsonPath("$.data.category").value("FOOD"))
                .andExpect(jsonPath("$.data.quantity").value(10));
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 조회 E2E 테스트")
    void getNonExistingProductE2ETest() throws Exception {
        // given
        Long nonExistingProductId = 999L;

        // when & then
        mockMvc.perform(get("/api/products/{productId}", nonExistingProductId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("상위 상품 랭킹 조회 E2E 테스트")
    void getTopProductsE2ETest() throws Exception {
        // when & then
        mockMvc.perform(get("/api/products/top"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Test Product 1"))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[1].productId").value(2))
                .andExpect(jsonPath("$.data[1].rank").value(2))
                .andExpect(jsonPath("$.data[2].productId").value(3))
                .andExpect(jsonPath("$.data[2].rank").value(3));
    }
}
