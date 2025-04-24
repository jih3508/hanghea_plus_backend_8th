package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.infrastructure.product.entity.ProductRank;
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


    public static ProductRankCommand from(DomainProductRank productRank){
        return ProductRankCommand.builder()
                .productId(productRank.getId())
                .name(productRank.getProductName())
                .rank(productRank.getRank())
                .totalQuantity(productRank.getTotalQuantity())
                .build();
    }

}
