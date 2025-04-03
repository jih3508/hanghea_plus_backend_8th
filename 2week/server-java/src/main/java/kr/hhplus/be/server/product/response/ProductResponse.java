package kr.hhplus.be.server.product.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class ProductResponse {

    private String name;
    private String productNumber;
    private BigDecimal price;
    private Integer stock;
}
