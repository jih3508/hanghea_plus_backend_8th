package kr.hhplus.be.server.interfaces.api.product.response;

import kr.hhplus.be.server.application.product.ProductInfoCommand;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ProductInfoResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private ProductCategory category;

    private Integer quantity;


    public static ProductInfoResponse toResponse(ProductInfoCommand command) {
        return ProductInfoResponse.builder()
                .id(command.getId())
                .name(command.getName())
                .price(command.getPrice())
                .category(command.getCategory())
                .quantity(command.getQuantity())
                .build();
    }

}
