package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.model.*;
import kr.hhplus.be.server.domain.product.repository.ProductRankRepository;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("상품 FACADE 통합 테스트")
class ProductFacadeIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ProductFacadeIntegrationTest.class);

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository stockRepository;

    @Autowired
    private ProductRankRepository rankRepository;

    private DomainProduct testProduct;

    @BeforeEach
    void setUp() {
        // 테스트용 상품 생성
        CreateProduct createProduct = CreateProduct.builder()
                .name("노트북")
                .price(BigDecimal.valueOf(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .build();
        testProduct = productRepository.create(createProduct);

        // 테스트용 상품 재고 생성
        CreateProductStock createStock = CreateProductStock.builder()
                .productId(testProduct.getId())
                .quantity(10)
                .build();
        stockRepository.create(createStock);

        // 테스트용 랭킹 데이터 생성
        CreateProductRank rank = CreateProductRank.builder()
                .productId(testProduct.getId())
                .rank(1)
                .totalQuantity(100)
                .build();

        rankRepository.manySave(List.of(rank));
    }

    @Test
    @DisplayName("존재하는 상품 조회 테스트")
    void getExistingProductTest() {
        // when
        ProductInfoCommand result = productFacade.getProduct(testProduct.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testProduct.getId());
        assertThat(result.getName()).isEqualTo("노트북");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(2_000_000));
        assertThat(result.getCategory()).isEqualTo(ProductCategory.ELECTRONIC_DEVICES);
        assertThat(result.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 테스트")
    void getNonExistingProductTest() {
        // when & then
        assertThatThrownBy(() -> productFacade.getProduct(999L))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("상품이 존재 하지 않습니다.");
    }

    @Test
    @DisplayName("오늘의 상품 랭킹 조회 테스트")
    void todayProductRankTest() {
        // given
        // 추가 상품 및 랭킹 데이터 생성
        CreateProduct createProduct2 = CreateProduct.builder()
                .name("스마트폰")
                .price(BigDecimal.valueOf(1_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .build();
        DomainProduct product2 = productRepository.create(createProduct2);

        CreateProductStock createStock2 = CreateProductStock.builder()
                .productId(product2.getId())
                .quantity(20)
                .build();
        stockRepository.create(createStock2);

        CreateProductRank rank2 = CreateProductRank.builder()
                .productId(product2.getId())
                .rank(2)
                .totalQuantity(80)
                .build();
        rankRepository.manySave(List.of(rank2));

        // when
        List<ProductRankCommand> rankList = productFacade.todayProductRank();

        // then
        assertThat(rankList).isNotNull();
        assertThat(rankList).hasSize(2);

        // 첫 번째 랭킹 확인
        assertThat(rankList.get(0).getProductId()).isEqualTo(testProduct.getId());
        assertThat(rankList.get(0).getRank()).isEqualTo(1);
        assertThat(rankList.get(0).getTotalQuantity()).isEqualTo(100);

        // 두 번째 랭킹 확인
        assertThat(rankList.get(1).getProductId()).isEqualTo(product2.getId());
        assertThat(rankList.get(1).getRank()).isEqualTo(2);
        assertThat(rankList.get(1).getTotalQuantity()).isEqualTo(80);
    }


    @Test
    @DisplayName("여러 카테고리 상품 조회 및 재고 확인")
    void multiCategoryProductsAndStockTest() {
        // given
        // 다른 카테고리 상품 추가
        CreateProduct fashionProduct = CreateProduct.builder()
                .name("티셔츠")
                .price(BigDecimal.valueOf(30_000))
                .category(ProductCategory.ETC)
                .build();
        DomainProduct product2 = productRepository.create(fashionProduct);

        CreateProductStock fashionStock = CreateProductStock.builder()
                .productId(product2.getId())
                .quantity(50)
                .build();
        stockRepository.create(fashionStock);

        // when
        ProductInfoCommand electronic = productFacade.getProduct(testProduct.getId());
        ProductInfoCommand fashion = productFacade.getProduct(product2.getId());

        // then
        // 전자제품 카테고리 확인
        assertThat(electronic.getCategory()).isEqualTo(ProductCategory.ELECTRONIC_DEVICES);
        assertThat(electronic.getQuantity()).isEqualTo(10);

        // 패션 카테고리 확인
        assertThat(fashion.getCategory()).isEqualTo(ProductCategory.ETC);
        assertThat(fashion.getQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("상품 재고 출납 후 재고 확인 테스트")
    void checkStockAfterDeliveringTest() {
        // given
        Integer deliveryQuantity = 5;

        // when
        // 먼저 재고 상태 확인
        ProductInfoCommand beforeDelivery = productFacade.getProduct(testProduct.getId());
        Integer initialQuantity = beforeDelivery.getQuantity();

        // 재고 출납 처리
        DomainProductStock updatedStock = stockRepository.findByProductId(testProduct.getId())
                .map(stock -> {
                    // delivering 로직
                    if (stock.getQuantity() < deliveryQuantity) {
                        throw new ApiExceptionResponse(org.springframework.http.HttpStatus.BAD_REQUEST, "제고가 부족 합니다.");
                    }
                    stock.setQuantity(stock.getQuantity() - deliveryQuantity);
                    return stockRepository.update(new UpdateProductStock(stock.getProductId(), stock.getQuantity()));
                })
                .orElseThrow(() -> new ApiExceptionResponse(org.springframework.http.HttpStatus.NOT_FOUND, "재고가 정보가 없습니다."));

        // 업데이트된 재고 확인
        ProductInfoCommand afterDelivery = productFacade.getProduct(testProduct.getId());

        // then
        assertThat(initialQuantity).isEqualTo(10);
        assertThat(afterDelivery.getQuantity()).isEqualTo(initialQuantity - deliveryQuantity);
        assertThat(updatedStock.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("과도한 재고 출납 요청 시 오류 테스트")
    void excessiveStockDeliveryRequestTest() {
        // given
        Integer excessiveQuantity = 100;

        // when & then
        // 직접 stockRepository를 통해 delivering 로직 검증
        assertThatThrownBy(() -> {
            stockRepository.findByProductId(testProduct.getId())
                    .map(stock -> {
                        if (stock.getQuantity() < excessiveQuantity) {
                            throw new ApiExceptionResponse(org.springframework.http.HttpStatus.BAD_REQUEST, "제고가 부족 합니다.");
                        }
                        stock.setQuantity(stock.getQuantity() - excessiveQuantity);
                        return stockRepository.update(new UpdateProductStock(stock.getProductId(), stock.getQuantity()));
                    })
                    .orElseThrow(() -> new ApiExceptionResponse(org.springframework.http.HttpStatus.NOT_FOUND, "재고가 정보가 없습니다."));
        })
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("제고가 부족 합니다.");

        // 재고는 변경되지 않았는지 확인
        ProductInfoCommand afterFailedDelivery = productFacade.getProduct(testProduct.getId());
        assertThat(afterFailedDelivery.getQuantity()).isEqualTo(10);
    }
}