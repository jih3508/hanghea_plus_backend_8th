package kr.hhplus.be.server.product.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigInteger;

@Getter
@Setter
@ToString
public class ProductTopResponse {

    private BigInteger productId;
    private String name;
    private Integer rank;
    private Integer salesCount;
}
