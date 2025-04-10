package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.ProductCategory;
import kr.hhplus.be.server.domain.product.entity.ProductStock;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {

    @InjectMocks
    private ProductFacade facade;

    @Mock
    private ProductService service;

    @Mock
    private ProductStockService stockService;

    @Test
    @DisplayName("조회시 상품에 대한 데이터가 없을 경우")
    void 상품_데이터X(){

        // given, when
        when(service.getProduct(anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "상품이 존재 하지 않습니다."));


        assertThatThrownBy(() -> facade.getProduct(anyLong()))
        .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("상품이 존재 하지 않습니다.");

        verify(stockService, never()).getStock(anyLong());

    }

    @Test
    @DisplayName("조회시 삳품에 대한 데이터가 있을 경우")
    void 상품_데이터O(){
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


        ProductInfoCommand expected = ProductInfoCommand.builder()
                .id(1L)
                .name("노트북")
                .price(new BigDecimal(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .quantity(10)
                .build();

        // when
        when(service.getProduct(1L)).thenReturn(product);
        when(stockService.getStock(1L)).thenReturn(stock);
        ProductInfoCommand result = facade.getProduct(1L);

        // then
        assertThat(result.getId()).isEqualTo(expected.getId());
        assertThat(result.getName()).isEqualTo(expected.getName());
        assertThat(result.getPrice()).isEqualTo(expected.getPrice());
        assertThat(result.getCategory()).isEqualTo(expected.getCategory());
        assertThat(result.getQuantity()).isEqualTo(expected.getQuantity());
        verify(service, times(1)).getProduct(1L);
        verify(stockService, times(1)).getStock(1L);

    }


}