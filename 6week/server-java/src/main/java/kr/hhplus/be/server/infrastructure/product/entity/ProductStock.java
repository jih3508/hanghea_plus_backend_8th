package kr.hhplus.be.server.infrastructure.product.entity;


import jakarta.persistence.*;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.model.CreateProductStock;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import lombok.*;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "product_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class ProductStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", unique = true)
    private Product product;


    private  Integer quantity;


    @Builder
    public ProductStock(Long id, Product product, Integer quantity) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
    }


    public static ProductStock create(CreateProductStock stock, Product product) {
        return ProductStock.builder()
                .product(product)
                .quantity(stock.getQuantity())
                .build();
    }

    public DomainProductStock toDomain(){
        return DomainProductStock.builder()
                .id(this.id)
                .productId(this.product.getId())
                .quantity(this.quantity)
                .name(this.product.getName())
                .price(this.product.getPrice())
                .category(this.product.getCategory())
                .build();
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
