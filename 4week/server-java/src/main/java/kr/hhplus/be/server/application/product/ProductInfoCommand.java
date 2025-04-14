package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.ProductCategory;
import kr.hhplus.be.server.domain.product.entity.ProductStock;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ProductInfoCommand {

    private Long id;

    private String name;

    private BigDecimal price;

    private ProductCategory category;

    private Integer quantity;


    public static ProductInfoCommand toCommand(Product product, ProductStock stock){
        return ProductInfoCommand.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .quantity(stock.getQuantity())
                .build();
    }

}
