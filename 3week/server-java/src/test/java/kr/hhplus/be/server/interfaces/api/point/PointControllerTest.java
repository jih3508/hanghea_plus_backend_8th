package kr.hhplus.be.server.interfaces.api.point;

import kr.hhplus.be.server.application.point.PointChargeCommand;
import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.interfaces.api.common.ControllerTest;
import kr.hhplus.be.server.interfaces.api.point.request.ChargeRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
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
    @DisplayName("없는 사용자 조회시 오류!!")
    void 포인트_조회_사용자X() throws Exception {
        Long userId = 100L;

        when(pointFacade.getPoint(anyLong())).thenThrow( new ApiExceptionResponse(HttpStatus.NOT_FOUND, "없는 사용자 입니다."));


        // when & then
        mockMvc.perform(
                get("/point/{userId}", userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("잔액 정상적으로 조회 처리")
    void 포인트_조회() throws Exception {
        // given
        Long userId = 1L;

        when(pointFacade.getPoint(anyLong())).thenReturn(new BigDecimal(1_000_000));

        // when & then
        mockMvc.perform(
                        get("/point/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(new BigDecimal(1_000_000)));
    }

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
        when(pointFacade.charge(any(PointChargeCommand.class)))
                .thenReturn(BigDecimal.valueOf(10_000));


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