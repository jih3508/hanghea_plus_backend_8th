package kr.hhplus.be.server.interfaces.api.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.application.product.ProductRankCommand;
import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.application.product.ProductInfoCommand;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.interfaces.api.common.ApiResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductInfoResponse;
import kr.hhplus.be.server.interfaces.api.product.response.ProductTopResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
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

    @Operation(summary = "상위 상품 조회 API")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/top")
    public ApiResponse<List<ProductTopResponse>> topProducts(){
        List<DomainProductRank> result = facade.todayProductRank();
        List<ProductRankCommand> command = result.stream().map(ProductRankCommand::from).collect(Collectors.toList());
        List<ProductTopResponse> responses = command.stream().map(ProductTopResponse::of).collect(Collectors.toList());
        return ApiResponse.ok(responses);

    }

}
