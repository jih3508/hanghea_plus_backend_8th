package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.application.coupon.CouponFacade;
import kr.hhplus.be.server.application.coupon.CouponIssueCommand;
import kr.hhplus.be.server.application.coupon.CouponMeCommand;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import kr.hhplus.be.server.interfaces.api.coupon.request.CouponIssueRequest;
import kr.hhplus.be.server.interfaces.api.coupon.response.CouponUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponFacade facade;


    @GetMapping("/{userId}")
    public ApiResponse<List<CouponUserResponse>> meCoupons(@PathVariable("userId") Long userId) {
        List<CouponMeCommand> commands = facade.getMeCoupons(userId);
        List<CouponUserResponse> responses = commands.stream().map(CouponUserResponse::of).collect(Collectors.toList());
        return ApiResponse.ok(responses);
    }


    @PostMapping("/issue/{userId}")
    public ApiResponse<String> issuedCoupon(@PathVariable("userId") Long userId,
                                          @RequestBody CouponIssueRequest request) {

        CouponIssueCommand command = CouponIssueCommand.of(userId, request.getCouponId());
        facade.issue(command);
        return ApiResponse.ok();
    }
}
