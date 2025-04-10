package kr.hhplus.be.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;


    @CreatedDate
    @Column(name = "create_date_time", updatable = false)
    private LocalDateTime createDateTime; // 생성 일시

    @LastModifiedDate
    @Column(name = "update_date_time")
    private LocalDateTime updateDateTime; // 수정 일시

    @Builder
    public Product(Long id, String name, BigDecimal price, ProductCategory category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;

    }

}
