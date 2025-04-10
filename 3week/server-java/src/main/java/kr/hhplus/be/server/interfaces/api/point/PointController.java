package kr.hhplus.be.server.interfaces.api.point;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.point.PointChargeCommand;
import kr.hhplus.be.server.application.point.PointFacade;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import kr.hhplus.be.server.interfaces.api.point.request.ChargeRequest;
import kr.hhplus.be.server.interfaces.api.point.response.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
@Tag(name="POINT", description = "포인트 API")
public class PointController {


    private final PointFacade pointFacade;

    @Operation(summary = "포인트 충전 API")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/charge/{userId}")
    public ApiResponse<PointResponse> charge(@PathVariable("userId") Long userId,
                                             @Valid @RequestBody ChargeRequest request){
        PointChargeCommand command = PointChargeCommand.of(userId, request);
        BigDecimal result = pointFacade.charge(command);
        return ApiResponse.ok(new PointResponse(result));

    }

    @Operation(summary = "포인트 조회 API")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public ApiResponse<PointResponse> get(@PathVariable("userId") Long userId){
        BigDecimal result = pointFacade.getPoint(userId);
        return ApiResponse.ok(new PointResponse(result));
    }
}
