package kr.hhplus.be.server.interfaces.api.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.application.product.ProductInfoCommand;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name="PRODUCT", description = "상품 API")
public class ProductController {

    private final ProductFacade facade;

    @Operation(summary = "삳품 조회 API")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{productId}")
    public ApiResponse<ProductInfoResponse> getProduct(@PathVariable Long productId){
        ProductInfoCommand  command = facade.getProduct(productId);
        ProductInfoResponse response = ProductInfoResponse.toResponse(command);
        return ApiResponse.ok(response);
    }

}
