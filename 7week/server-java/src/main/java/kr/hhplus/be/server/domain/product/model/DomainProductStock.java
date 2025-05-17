package kr.hhplus.be.server.domain.product.model;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class DomainProductStock {

    private Long id;

    private Long productId;

    private  Integer quantity;

    private String name; // 상품명

    private BigDecimal price; // 가격

    private ProductCategory category;

    /*
     * method: isStock
     * description: 재고 여부
     */
    public Boolean isStock(){
        return quantity > 0;
    }

    /*
     * method: stockReceiving
     * description: 재고 입고
     */
    public void stockReceiving(Integer quantity){
        this.quantity += quantity;
    }

    /*
     * method: stockDelivering
     * description: 재고 출납
     */
    public void stockDelivering(Integer quantity){
        if(this.quantity - quantity < 0){
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "제고가 부족 합니다.");
        }

        this.quantity -= quantity;
    }

}
