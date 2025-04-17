package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import kr.hhplus.be.server.infrastructure.product.entity.Product;
import kr.hhplus.be.server.infrastructure.product.entity.ProductCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {

    @InjectMocks
    private ProductFacade facade;

    @Mock
    private ProductService service;

    @Mock
    private ProductStockService stockService;

    @Mock
    private ProductRankService rankService;

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
        DomainProduct product = DomainProduct.builder()
                .id(1L)
                .name("노트북")
                .price(new BigDecimal(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
                .build();

        DomainProductStock stock = DomainProductStock.builder()
                .id(1L)
                .productId(1L)
                .name("노트북")
                .price(new BigDecimal(2_000_000))
                .category(ProductCategory.ELECTRONIC_DEVICES)
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


    @Test
    @DisplayName("상위 상품 조회 테스트")
    void 상위_상품(){

        // given
        Product product1 = Product.builder()
                .id(1L)
                .name("상품1")
                .build();

        Product product2= Product.builder()
                .id(12L)
                .name("상품2")
                .build();


        Product product3= Product.builder()
                .id(3L)
                .name("상품3")
                .build();

        DomainProductRank rank1 = DomainProductRank.builder()
                .id(1L)
                .productId(product1.getId())
                .rank(1)
                .totalQuantity(100)
                .build();


        DomainProductRank rank2 = DomainProductRank.builder()
                .id(2L)
                .productId(product2.getId())
                .rank(2)
                .totalQuantity(70)
                .build();


        DomainProductRank rank3 = DomainProductRank.builder()
                .id(2L)
                .productId(product3.getId())
                .rank(3)
                .totalQuantity(50)
                .build();


        List<DomainProductRank> ranks = List.of(rank1, rank2, rank3);

        //when
        when(rankService.todayProductRank()).thenReturn(ranks);

        List<ProductRankCommand> commands = facade.todayProductRank();

        assertThat(commands.size()).isEqualTo(3);

    }

}