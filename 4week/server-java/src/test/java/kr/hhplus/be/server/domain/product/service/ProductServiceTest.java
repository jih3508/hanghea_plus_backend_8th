package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;


    @Test
    @DisplayName("조회시 테이블에 상품이 없을 경우")
    void 테이블_상품_데이터_없음(){

        // given, when
        when(repository.findById(anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "상품이 존재 하지 않습니다."));

        assertThatThrownBy(() -> service.getProduct(anyLong()))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("상품이 존재 하지 않습니다.");
    }

    @Test
    @DisplayName("조회시 테이블에 상품이 있을경우 상품 정보 가져온다.")
    void 테이블_상품_데이터_있음(){
        // given
        Product product = Product.builder()
                .id(1L)
                .name("노트북")
                .price(new BigDecimal(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .build();

        //when
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(product));
        Product result = service.getProduct(1L);

        assertThat(result).isEqualTo(product);

    }

}