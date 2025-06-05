package kr.hhplus.be.server.domain.product.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DomainProductRank {

    private Long id;

    private Long productId;

    private String productName;

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal price;

    private ProductCategory category;

    private Integer rank;

    private Integer totalQuantity;

    public static DomainProductRank of(Product product, Integer rank, Integer totalQuantity) {
        return DomainProductRank.builder()
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .rank(rank)
                .totalQuantity(totalQuantity)
                .build();
    }
}
