package kr.hhplus.be.server.interfaces.api.product.response;

import kr.hhplus.be.server.application.point.ProductRankCommand;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ProductTopResponse {

    private Long productId;

    private String name;

    private Integer rank;

    private Integer totalCount;

    public static  ProductTopResponse of(ProductRankCommand command){
        return ProductTopResponse.builder()
                .productId(command.getProductId())
                .name(command.getName())
                .rank(command.getRank())
                .totalCount(command.getTotalQuantity())
                .build();
    }
}
