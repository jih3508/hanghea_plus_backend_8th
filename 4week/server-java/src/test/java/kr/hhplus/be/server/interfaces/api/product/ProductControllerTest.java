package kr.hhplus.be.server.interfaces.api.product;

import kr.hhplus.be.server.application.product.ProductRankCommand;
import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.application.product.ProductInfoCommand;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.entity.ProductCategory;
import kr.hhplus.be.server.interfaces.api.common.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends ControllerTest {


    @MockitoBean
    private ProductFacade productFacade;

    @DisplayName("없는 상품 조회 실패 한다.")
    @Test
    void 없는_상품_조회() throws Exception {

        // given
        when(productFacade.getProduct(anyLong()))
                .thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "상품이 존재 하지 않습니다."));

        mockMvc.perform(
                get("/products/{productId}", anyLong())
        ).andExpect(status().isNotFound())
                .andDo(print());
    }

    @DisplayName("상품 조회를 한다.")
    @Test
    void 상품_조회()  throws Exception {
        // given
        ProductInfoCommand command = ProductInfoCommand.builder()
                .id(1L)
                .name("노트북")
                .price(new BigDecimal(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .quantity(10)
                .build();

        when(productFacade.getProduct(1L)).thenReturn(command);

        mockMvc.perform(
                get("/products/{productId}", 1L)
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("노드북"))
                .andDo(print());


    }

    @Test
    @DisplayName("상위 상품 조회 테스트")
    void 상위_상품() throws Exception {
        // given
        ProductRankCommand rank1 = ProductRankCommand.builder()
                .productId(1l)
                .name("상품 A")
                .rank(1)
                .totalQuantity(200)
                .build();

        ProductRankCommand rank2 = ProductRankCommand.builder()
                .productId(2l)
                .name("상품 B")
                .rank(2)
                .totalQuantity(100)
                .build();

        ProductRankCommand rank3 = ProductRankCommand.builder()
                .productId(3l)
                .name("상품 C")
                .rank(1)
                .totalQuantity(90)
                .build();

        List<ProductRankCommand> mockRanks = List.of(rank1, rank2);

        // When & Then
        mockMvc.perform(get("/products/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("상품 A"))
                .andExpect(jsonPath("$.data[0].price").value(10000))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].quantity").value(50))
                .andExpect(jsonPath("$.data[1].productId").value(2))
                .andExpect(jsonPath("$.data[1].rank").value(2));

    }

}