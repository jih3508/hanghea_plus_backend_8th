package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.model.CreateProduct;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("상품 서비스 통합 테스트")
class ProductServiceIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceIntegrationTest.class);

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        CreateProduct createProduct = CreateProduct.builder()
                .name("노트북")
                .price(BigDecimal.valueOf(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .build();

        repository.create(createProduct);

    }

    @Test
    @DisplayName("없는 상품 조회시 오류가 난다.")
    void 상품X_조회(){
        // given
        Long productId = 100L;

        assertThatThrownBy(()-> service.getProduct(productId))
        .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("상품이 존재 하지 않습니다.");
    }

    @Test
    @DisplayName("있는 상품 조회시 상품 class가 있다.")
    void 상품O_조회(){
        Long productId = 1L;

        DomainProduct product = service.getProduct(productId);

        assertThat(product).isNotNull();
    }


}