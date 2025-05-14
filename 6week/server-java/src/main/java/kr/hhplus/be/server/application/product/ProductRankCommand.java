package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class ProductRankCommand implements Serializable {

    private Long productId;

    private String name;

    private Integer rank;

    private Integer totalQuantity;


    public static ProductRankCommand from(DomainProductRank productRank){
        return ProductRankCommand.builder()
                .productId(productRank.getProductId())
                .name(productRank.getProductName())
                .rank(productRank.getRank())
                .totalQuantity(productRank.getTotalQuantity())
                .build();
    }

}
