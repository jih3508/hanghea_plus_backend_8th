package kr.hhplus.be.server.application.product;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class ProductRankCommand{

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
