package kr.hhplus.be.server.interfaces.api.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.hhplus.be.server.application.order.OrderCommand;
import kr.hhplus.be.server.application.order.OrderFacade;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import kr.hhplus.be.server.interfaces.api.order.request.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name="ORDER", description = "주문 API")
public class OrderController {

    private final OrderFacade facade;

    @Operation(summary = "주문 결제 API")
    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<String> order(@PathVariable("userId") Long userId,
                               @Valid @RequestBody OrderRequest request){

        OrderCommand command = OrderCommand.toCommand(userId, request);
        String requestId = facade.orderAsync(command);
        return ApiResponse.of("주문이 접수되었습니다. 결과는 실시간으로 알림됩니다.", requestId);
    }

}
