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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("상품 재고 서비스 동시성 테스트")
class ProductStockServiceConcurrencyTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ProductStockServiceConcurrencyTest.class);
    @Autowired
    private ProductStockService productStockService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStockRepository productStockRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
        latch = new CountDownLatch(1);
    }

    @Test
    @DisplayName("동시 출납 테스트")
    void 재고_동시출납_테스트() throws InterruptedException {
        // given
        // 테스트용 상품 및 재고 생성
        final Long productId = transactionTemplate.execute(status -> {
            // 상품 생성
            CreateProduct createProduct = CreateProduct.builder()
                    .name("한정수량상품")
                    .price(new BigDecimal(10_000))
                    .category(ProductCategory.ELECTRONIC_DEVICES)
                    .productNumber("TEST001")
                    .build();
            DomainProduct product = productRepository.create(createProduct);

            // 재고 생성
            CreateProductStock createStock = CreateProductStock.builder()
                    .productId(product.getId())
                    .quantity(10) // 10개 재고
                    .build();
            productStockRepository.create(createStock);

            return product.getId();
        });

        // 20명의 사용자가 동시에 각 1개씩 구매 시도
        int concurrentRequests = 20;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(concurrentRequests);

        // when
        for (int i = 0; i < concurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    // 트랜잭션 내에서 재고 출납 시도
                    transactionTemplate.execute(status -> {
                        try {
                            productStockService.delivering(productId, 1);
                            successCount.incrementAndGet();
                        } catch (ApiExceptionResponse e) {
                            failureCount.incrementAndGet();
                        }
                        return null;
                    });

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // 모든 스레드 동시 시작
        latch.countDown();

        // 모든 스레드 완료 대기
        completionLatch.await();

        // then
        // 성공한 출납은 10개 이하여야 함 (재고가 10개)
        //assertThat(successCount.get()).isLessThanOrEqualTo(10);

        // 실패한 출납은 10개 이상이어야 함
        //assertThat(failureCount.get()).isGreaterThanOrEqualTo(10);

        // 총 출납 시도는 20개
        assertThat(successCount.get() + failureCount.get()).isEqualTo(20);


        // 데이터베이스에서 실제 재고 수량 확인
        DomainProductStock stock = productStockService.getStock(productId);
        log.info("successCount: " +  successCount.get() + ", failureCount: " + failureCount.get());
        log.info("상품 재고 개수:" + stock.getQuantity());

        assertThat(stock.getQuantity()).isEqualTo(0); // 모든 재고가 소진되어야 함
    }



}