package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
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


    public static ProductInfoCommand toCommand(DomainProductStock stock){
        return ProductInfoCommand.builder()
                .id(stock.getProductId())
                .name(stock.getName())
                .price(stock.getPrice())
                .category(stock.getCategory())
                .quantity(stock.getQuantity())
                .build();
    }

}
