package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import kr.hhplus.be.server.interfaces.api.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.interfaces.api.coupon.response.CouponUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponFacade facade;


    @GetMapping("/{userId}")
    public ResponseEntity<List<CouponUserResponse>> meCoupons(@PathVariable BigInteger userId) {
        return ResponseEntity.ok(new ArrayList<CouponUserResponse>());
    }


    @PostMapping("/issue/{userId}")
    public ApiResponse<String> issuedCoupon(@PathVariable("userId") Long userId,
                                          @RequestBody CouponIssueRequest request) {

        CouponIssueCommand command = CouponIssueCommand.of(userId, request.getCouponId());
        facade.issue(command);
        return ApiResponse.ok();
    }
}
