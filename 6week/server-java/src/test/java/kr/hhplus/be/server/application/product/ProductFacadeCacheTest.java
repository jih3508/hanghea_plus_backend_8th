package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.support.RedisCleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductFacadeCacheTest{

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private RedisCleanup redisCleanup;

    @Mock
    private ProductRankService productRankService;

    @BeforeEach
    void setUp() {
        redisCleanup.flushAll();
    }

    @Test
    @DisplayName("todayProductRank 메서드 캐시 적용 테스트")
    public void 상품_랭킹_캐시() {
        // given: mock 데이터 준비
        List<DomainProductRank> mockRanks = new ArrayList<>();
        when(productRankService.todayProductRank()).thenReturn(mockRanks);

        // when: 동일한 메서드를 여러 번 호출
        productFacade.todayProductRank(); // 첫 번째 호출 - 캐시 저장
        productFacade.todayProductRank(); // 두 번째 호출 - 캐시에서 조회
        productFacade.todayProductRank(); // 세 번째 호출 - 캐시에서 조회

        // then: 서비스는 한 번만 호출되어야 함 (캐시가 적용되었기 때문)
        verify(productRankService, times(1)).todayProductRank();
    }



}