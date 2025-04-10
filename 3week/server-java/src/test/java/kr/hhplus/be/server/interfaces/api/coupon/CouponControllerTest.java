package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.interfaces.api.common.ControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

}