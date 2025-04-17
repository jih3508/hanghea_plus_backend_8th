package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.application.coupon.CouponMeCommand;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.infrastructure.coupon.entity.CouponType;
import kr.hhplus.be.server.interfaces.api.common.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CouponControllerTest extends ControllerTest {


    @MockitoBean
    private CouponFacade couponFacade;

    @Test
    @DisplayName("쿠폰 개수 부족해서 발급 실패 한다.")
    void 쿠폰발급실패() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 100L;
        String requestBody = """
            {
                "couponId": 100
            }
        """;

        CouponIssueCommand command = CouponIssueCommand.of(userId, couponId);

        doThrow(new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "발급할 쿠폰이 없습니다."))
                .when(couponFacade).issue(command);


        // when & then
        mockMvc.perform(post("/coupons/issue/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쿠폰발급() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 100L;
        String requestBody = """
            {
                "couponId": 100
            }
        """;

        // when & then
        mockMvc.perform(post("/coupons/issue/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(couponFacade, times(1)).issue(CouponIssueCommand.of(userId, couponId));
    }

    @Test
    @DisplayName("쿠폰 3개 조회 테스트")
    void 조회_테스트() throws Exception {
        // given
        Long userId = 1L;
        List<CouponMeCommand> couponList = List.of(
                CouponMeCommand.builder()
                        .couponId(101L)
                        .couponNumber("COUPON101")
                        .type(CouponType.FLAT)
                        .discountPrice(BigDecimal.valueOf(1000))
                        .rate(null)
                        .isUsed(false)
                        .build(),
                CouponMeCommand.builder()
                        .couponId(102L)
                        .couponNumber("COUPON102")
                        .type(CouponType.RATE)
                        .discountPrice(null)
                        .rate(10)
                        .isUsed(false)
                        .build(),
                CouponMeCommand.builder()
                        .couponId(103L)
                        .couponNumber("COUPON103")
                        .type(CouponType.FLAT)
                        .discountPrice(BigDecimal.valueOf(2000))
                        .rate(null)
                        .isUsed(true)
                        .build()
        );

        when(couponFacade.getMeCoupons(userId)).thenReturn(couponList);

        // when & then
        mockMvc.perform(get("/coupons/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].couponNumber").value("COUPON101"))
                .andExpect(jsonPath("$.data[1].type").value("RATE"))
                .andExpect(jsonPath("$.data[2].isUsed").value(true));
    }


}