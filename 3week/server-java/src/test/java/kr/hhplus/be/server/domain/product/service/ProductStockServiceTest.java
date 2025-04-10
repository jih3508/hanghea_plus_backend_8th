package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.ProductCategory;
import kr.hhplus.be.server.domain.product.entity.ProductStock;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductStockServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ProductStockServiceTest.class);

    @InjectMocks
    private ProductStockService service;

    @Mock
    private ProductStockRepository repository;

    @Test
    @DisplayName("조회시 테이블에 재고 관련 데이터가 없을때")
    void 테이블_제고_정보_없음(){

        //given && when
        when(repository.findByProductId(anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "재고가 없습니다."));

        // then
        assertThatThrownBy(()-> service.getStock(anyLong()))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("재고가 없습니다.");
    }

    @Test
    @DisplayName("조회시 테이블에 재고 관련 데이터가 있을때")
    void 테이블_제고_정보_있음(){
        // given
        Product product = Product.builder()
                .id(1L)
                .name("노트북")
                .price(new BigDecimal(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .build();

        ProductStock stock = ProductStock.builder()
                .id(1L)
                .product(product)
                .quantity(10)
                .build();

        // when
        when(repository.findByProductId(anyLong())).thenReturn(Optional.of(stock));
        ProductStock result = service.getStock(1L);

        // then
        assertThat(result).isEqualTo(stock);

    }

}