package kr.hhplus.be.server.infrastructure.product.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.product.model.CreateProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Table(name = "product_rank")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="product_id")
    private Product product;

    @Column(name = "rank_date")
    @CreatedDate
    private LocalDate rankDate;

    private Integer rank;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

    @Builder
    public ProductRank(Long id, Product product, LocalDate rankDate, Integer rank, Integer totalQuantity) {
        this.id = id;
        this.product = product;
        this.rankDate = LocalDate.now();
        this.rank = rank;
        this.totalQuantity = totalQuantity;
    }

    public static ProductRank create(CreateProductRank create, Product product) {
        return ProductRank.builder()
                .product(product)
                .rankDate(LocalDate.now())
                .rank(create.getRank())
                .totalQuantity(create.getTotalQuantity())
                .build();
    }

    public DomainProductRank toDomain(){
        return DomainProductRank.builder()
                .id(this.id)
                .productId(this.product.getId())
                .productName(this.product.getName())
                .price(this.product.getPrice())
                .category(this.product.getCategory())
                .totalQuantity(this.totalQuantity)
                .rank(this.rank)
                .totalQuantity(this.totalQuantity)
                .build();
    }


}
