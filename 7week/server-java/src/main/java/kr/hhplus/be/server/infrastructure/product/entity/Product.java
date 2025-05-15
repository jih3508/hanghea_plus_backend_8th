package kr.hhplus.be.server.infrastructure.product.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.product.model.CreateProduct;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "product_number", unique = true)
    private String productNumber;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;


    @CreatedDate
    @Column(name = "created_date_time", updatable = false)
    private LocalDateTime createDateTime; // 생성 일시

    @LastModifiedDate
    @Column(name = "updated_date_time")
    private LocalDateTime updateDateTime; // 수정 일시

    @Builder
    public Product(Long id, String name, String productNumber, BigDecimal price, ProductCategory category) {
        this.id = id;
        this.name = name;
        this.productNumber = productNumber;
        this.price = price;
        this.category = category;
        this.createDateTime = LocalDateTime.now();

    }

    public static Product create(CreateProduct product) {
        return Product.builder()
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .productNumber(product.getProductNumber())
                .build();
    }

    public DomainProduct toDomain(){
        return DomainProduct.builder()
                .id(this.id)
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .build();
    }

}
