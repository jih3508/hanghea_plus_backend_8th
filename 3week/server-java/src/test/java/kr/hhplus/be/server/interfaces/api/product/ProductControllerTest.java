package kr.hhplus.be.server.interfaces.api.product;

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

}