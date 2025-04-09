package kr.hhplus.be.server.balance;

import kr.hhplus.be.server.balance.response.BalanceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/user/{userId}")
public class BalanceController {

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String userId) throws Exception {
        return  ResponseEntity.ok(BalanceResponse.of(new BigDecimal("100000")));
    }

    @PostMapping("/balance/charge")
    public ResponseEntity<Void> charge(@PathVariable String userId, @RequestBody BigDecimal amount) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
