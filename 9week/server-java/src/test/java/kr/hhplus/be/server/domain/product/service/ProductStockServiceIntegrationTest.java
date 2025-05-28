package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.model.CreateProduct;
import kr.hhplus.be.server.domain.product.model.CreateProductStock;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("포인트 이력 서비스 통합 테스트")
class ProductStockServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private ProductStockService service;

    @Autowired
    private ProductStockRepository repository;

    @Autowired
    private ProductRepository productRepository;


    @BeforeEach
    void setup() {
        CreateProduct createProduct = CreateProduct.builder()
                .name("노트북")
                .price(BigDecimal.valueOf(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .build();

        DomainProduct product = productRepository.create(createProduct);

        CreateProductStock createStock = CreateProductStock.builder()
                .productId(product.getId())
                .quantity(10)
                .build();
        repository.create(createStock);

    }

    @Test
    @DisplayName("없는 상품 조회시 오류가 난다.")
    void 상품X_조회() {
        // given
        Long productId = 100L;

        // when & then
        assertThatThrownBy(() -> service.getStock(productId))
        .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("재고가 정보가 없습니다.");

    }

    @Test
    @DisplayName("있는 상품 조회시 재고 정보가 조회가 된다.")
    void 상품O_조회(){
        // given
        Long productId = 100L;

        // when
        DomainProductStock result = service.getStock(productId);

        // then
        assertThat(result).isNotNull();
    }


    @Test
    @DisplayName("재고가 부족한 상태에서 출납처리 하면 출납을 할수가 없다.")
    void 재고_부족시_출납(){
        // given
        Long productId = 1L;
        Integer quantity = 100;

        // when, then
        assertThatThrownBy(() -> service.delivering(productId, quantity))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("제고가 부족 합니다.");


    }

    @Test
    @DisplayName("재고가 충분한 상태에서 출납처리하면 정상적으로 출납할 수 있다.")
    void 재고_충분_출납(){
        //given
        Long productId = 1L;
        Integer quantity = 5;
        DomainProductStock stock = service.getStock(productId);


        //when
        DomainProductStock result = service.delivering(productId, quantity);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(stock.getQuantity() - quantity);
    }

}