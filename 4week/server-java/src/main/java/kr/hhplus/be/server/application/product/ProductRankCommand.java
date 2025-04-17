package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.entity.ProductRank;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ProductRankCommand {

    private Long productId;

    private String name;

    private Integer rank;

    private Integer totalQuantity;


    public static ProductRankCommand from(ProductRank productRank){
        return ProductRankCommand.builder()
                .productId(productRank.getProduct().getId())
                .name(productRank.getProduct().getName())
                .rank(productRank.getRank())
                .totalQuantity(productRank.getTotalQuantity())
                .build();
    }

}
