package kr.hhplus.be.server.interfaces.api.order;

import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.interfaces.api.common.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest extends ControllerTest {

    @MockitoBean
    private OrderFacade orderFacade;

    @Test
    void 주문_정상_테스트() throws Exception {
        // Given
        Long userId = 1L;
        String requestJson = """
            {
                "items": [
                    {
                        "productId": 100,
                        "quantity": 2,
                        "couponId": 10
                    }
                ]
            }
        """;


        // When & Then
        mockMvc.perform(post("/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

    }

    @Test
    void 주문_수량_누략() throws Exception {
        // Given: quantity 누락
        Long userId = 1L;
        String requestJson = """
            {
                "items": [
                    {
                        "productId": 100,
                        "couponId": 10
                    }
                ]
            }
        """;



        // When & Then
        mockMvc.perform(post("/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());

    }


    @Test
    @DisplayName("주문 결제 진행시 요류 날 경우")
    void 진행시_오류_테스트() throws Exception {
        // Given
        Long userId = 1L;
        String requestJson = """
            {
                "items": [
                    {
                        "productId": 100,
                        "quantity": 2,
                        "couponId": 10
                    }
                ]
            }
        """;

        // facade.order 호출 시 예외 발생하도록 설정
        doThrow(new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "쿠폰 기간 만료 되었습니다."))
                .when(orderFacade).order(any(OrderCommand.class));

        // When & Then
        mockMvc.perform(post("/orders/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isInternalServerError());
    }


}