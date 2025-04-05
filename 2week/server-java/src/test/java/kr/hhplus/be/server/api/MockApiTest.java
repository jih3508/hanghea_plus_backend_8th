package kr.hhplus.be.server.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MockApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testChargeBalance() throws Exception {
        mockMvc.perform(post("/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1, \"amount\": 5000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Balance charged successfully"));
    }

    @Test
    public void testGetBalance() throws Exception {
        mockMvc.perform(get("/balance/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());
    }

    @Test
    public void testIssueCoupon() throws Exception {
        mockMvc.perform(post("/coupon/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Coupon issued successfully"));
    }

    @Test
    public void testCreateOrder() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user1\", \"items\": [{\"productId\": 1, \"quantity\": 2}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order created successfully"));
    }

    @Test
    public void testGetProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").exists())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    public void testGetTopProducts() throws Exception {
        mockMvc.perform(get("/products/top"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").exists())
                .andExpect(jsonPath("$[0].salesCount").exists());
    }

    @Test
    public void testChargeBalanceFailure() throws Exception {
        mockMvc.perform(post("/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user1\", \"amount\": -100}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testOrderFailureDueToLowBalance() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"user1\", \"items\": [{\"productId\": 1, \"quantity\": 100}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetInvalidUserBalance() throws Exception {
        mockMvc.perform(get("/balance/100"))
                .andExpect(status().isNotFound());
    }

}
