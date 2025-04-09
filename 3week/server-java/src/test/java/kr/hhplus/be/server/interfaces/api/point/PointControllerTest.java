package kr.hhplus.be.server.interfaces.api.point;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.hhplus.be.server.application.point.PointChargeCommand;
import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import kr.hhplus.be.server.interfaces.api.common.ControllerTest;
import kr.hhplus.be.server.interfaces.api.point.request.ChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.response.PointResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PointControllerTest extends ControllerTest {

    private static final Logger log = LoggerFactory.getLogger(PointControllerTest.class);

    @MockitoBean
    private PointFacade pointFacade;


    @Test
    @DisplayName("충전시 amount는 필수로 넣어야 한다.")
    void 포인트_amount_생략() throws Exception{
        // given
        ChargeRequest request = new ChargeRequest();
        Long userId = 1L;

        // when & then
        mockMvc.perform(
                post("/point/charge/{userId}", userId)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNotAcceptable());
                //.andExpect(jsonPath("$.error").exists());
                //.andExpect(jsonPath("$.message").value("충전 금액은 필수 값 입니다."));

    }

    @ParameterizedTest
    @DisplayName("충전시 amount는 양수가 아니면 실패 되도록 한다.")
    @ValueSource(longs = {-1000, 0})
    void 포인트_amount_음수(long amount) throws Exception{
        // given
        Long userId = 1L;
        ChargeRequest request = new ChargeRequest();
        request.setAmount(BigDecimal.valueOf(amount));

        // when & then
        mockMvc.perform(post("/point/charge/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotAcceptable());
    }

    @Test
    @DisplayName("잔액 정상적인 충전")
    void 잔액_충전() throws Exception {
        // given
        Long userId = 1L;
        ChargeRequest request = new ChargeRequest(BigDecimal.valueOf(10_000));
        given(pointFacade.charge(any(PointChargeCommand.class)))
                .willReturn(BigDecimal.valueOf(10_000));


        log.info(objectMapper.writeValueAsString(request));

        //when & then

        mockMvc.perform(post("/point/charge/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(BigDecimal.valueOf(10_000)));

    }



}