package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.interfaces.api.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.support.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CouponControllerE2ETest extends E2ETest {

    @Test
    @DisplayName("사용자 쿠폰 목록 조회 E2E 테스트")
    void getUserCouponsE2ETest() throws Exception {
        // given
        Long userId = 1L; // 사용자 1은 이미 쿠폰을 가지고 있음

        // when & then
        mockMvc.perform(get("/api/coupons/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].couponId").value(1))
                .andExpect(jsonPath("$.data[0].couponNumber").value("FLAT500"))
                .andExpect(jsonPath("$.data[0].type").value("FLAT"))
                .andExpect(jsonPath("$.data[0].discountPrice").value(500))
                .andExpect(jsonPath("$.data[0].isUsed").value(false));
    }

    @Test
    @DisplayName("쿠폰 발급 E2E 테스트")
    void issueCouponE2ETest() throws Exception {
        // given
        Long userId = 2L; // 사용자 2는 아직 쿠폰을 가지고 있지 않음
        CouponIssueRequest request = new CouponIssueRequest();
        request.setCouponId(2L); // RATE10 쿠폰

        // when & then
        // 1. 쿠폰 발급
        mockMvc.perform(post("/api/coupons/issue/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));

        // 2. 발급된 쿠폰 확인
        mockMvc.perform(get("/api/coupons/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].couponId").value(2))
                .andExpect(jsonPath("$.data[0].couponNumber").value("RATE10"))
                .andExpect(jsonPath("$.data[0].type").value("RATE"))
                .andExpect(jsonPath("$.data[0].rate").value(10))
                .andExpect(jsonPath("$.data[0].isUsed").value(false));
    }

    @Test
    @DisplayName("재고가 없는 쿠폰 발급 시도 E2E 테스트")
    void issueOutOfStockCouponE2ETest() throws Exception {
        // given
        Long userId = 2L;
        CouponIssueRequest request = new CouponIssueRequest();
        request.setCouponId(999L); // 존재하지 않는 쿠폰

        // when & then
        mockMvc.perform(post("/api/coupons/issue/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 쿠폰 조회 E2E 테스트")
    void getNonExistingUserCouponsE2ETest() throws Exception {
        // given
        Long nonExistingUserId = 999L;

        // when & then
        mockMvc.perform(get("/api/coupons/{userId}", nonExistingUserId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
