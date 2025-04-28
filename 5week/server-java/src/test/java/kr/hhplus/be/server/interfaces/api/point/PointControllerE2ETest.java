package kr.hhplus.be.server.interfaces.api.point;

import kr.hhplus.be.server.interfaces.api.point.request.ChargeRequest;
import kr.hhplus.be.server.support.E2ETest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"/sql/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PointControllerE2ETest extends E2ETest {

    @Test
    @DisplayName("포인트 조회 E2E 테스트")
    void getPointE2ETest() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(get("/api/point/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.amount").value(10000));
    }

    @Test
    @DisplayName("포인트 충전 E2E 테스트")
    void chargePointE2ETest() throws Exception {
        // given
        Long userId = 1L;
        ChargeRequest request = new ChargeRequest(BigDecimal.valueOf(5000));

        // when & then
        mockMvc.perform(post("/api/point/charge/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.amount").value(15000)); // 10000(initial) + 5000(charged)
    }

    @Test
    @DisplayName("포인트 충전 - 음수 금액 실패 E2E 테스트")
    void chargePointWithNegativeAmountE2ETest() throws Exception {
        // given
        Long userId = 1L;
        ChargeRequest request = new ChargeRequest(BigDecimal.valueOf(-5000));

        // when & then
        mockMvc.perform(post("/api/point/charge/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 포인트 조회 E2E 테스트")
    void getNonExistingUserPointE2ETest() throws Exception {
        // given
        Long nonExistingUserId = 999L;

        // when & then
        mockMvc.perform(get("/api/point/{userId}", nonExistingUserId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
