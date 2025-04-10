package kr.hhplus.be.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @Column(name ="product_id")
    private Product product;

    @Column(name = "rankDate")
    @CreatedDate
    private LocalDate rankDate;

    private Integer rank;

    private Integer totalQuantity;

    @Builder
    public ProductRank(Long id, Product product, LocalDate rankDate, Integer rank, Integer totalQuantity) {
        this.id = id;
        this.product = product;
        this.rankDate = LocalDate.now();
        this.rank = rank;
        this.totalQuantity = totalQuantity;
    }


}
