package kr.hhplus.be.server.domain.product.entity;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import kr.hhplus.be.server.infrastructure.product.entity.ProductStock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProductStockTest {

    private static final Logger log = LoggerFactory.getLogger(ProductStockTest.class);


    @Test
    @DisplayName("재고가 있을 경우 이용 가능 테스트")
    void 재고_있음(){
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

        // when && then
        assertThat(stock.isStock()).isTrue();
    }

    @Test
    @DisplayName("재고가 없음 경우 이용 불가능 테스트")
    void 재고_없음(){
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
                .quantity(0)
                .build();

        // when & then
        assertThat(stock.isStock()).isFalse();
    }


    @Test
    @DisplayName("재고가 입고 될 경우 재고가 수량이 추가 되어야 한다.")
    void 재고_입고(){
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
                .quantity(5)
                .build();

        int quantity = 7;

        // when
        stock.stockReceiving(quantity);

        // then
        assertThat(stock.getQuantity()).isEqualTo(quantity + 5);
    }

    @Test
    @DisplayName("재고 출납시 재고가 부족하면 부족하다고 오류 보낸다.")
    void 재고_출납_부족(){
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
                .quantity(5)
                .build();

        int quantity = 7;

        assertThatThrownBy(() -> stock.stockDelivering(quantity))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("제고가 부족 합니다.");
    }

    @Test
    @DisplayName("재고 출납시 재고가 출납이 되고 출납된 재고 개수가 반영 되어야 한다.")
    void 재고_출납(){
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

        int quantity = 7;

        stock.stockDelivering(quantity);

        assertThat(stock.getQuantity()).isEqualTo(10 -quantity);
    }


}