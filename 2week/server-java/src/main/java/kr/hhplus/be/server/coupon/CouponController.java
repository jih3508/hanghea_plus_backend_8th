package kr.hhplus.be.server.coupon;

import kr.hhplus.be.server.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.coupon.response.CouponUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CouponController {


    @GetMapping("user/{userId}/coupons")
    public ResponseEntity<List<CouponUserResponse>> meCoupons(@PathVariable BigInteger userId) {
        return ResponseEntity.ok(new ArrayList<CouponUserResponse>());
    }

    @PostMapping("/coupons/{couponId}")
    public ResponseEntity<Void> issuedCoupon(@PathVariable BigInteger couponId,
    @RequestBody CouponIssueRequest request) {
        return ResponseEntity.ok().build();
    }
}
