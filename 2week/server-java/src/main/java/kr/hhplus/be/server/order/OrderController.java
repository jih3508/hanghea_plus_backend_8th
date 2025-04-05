package kr.hhplus.be.server.order;

import kr.hhplus.be.server.order.request.OrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@RequestMapping("/order")
public class OrderController {

    @PostMapping("")
    public ResponseEntity<BigInteger> createOrder(@RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(new BigInteger("1"));
    }
}
