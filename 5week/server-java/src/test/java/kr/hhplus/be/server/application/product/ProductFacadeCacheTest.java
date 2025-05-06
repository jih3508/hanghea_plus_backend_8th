package kr.hhplus.be.server.application.product;

import kr.hhplus.be.server.domain.product.model.DomainProductRank;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * ProductFacade 캐시 동작 테스트
 * @Cacheable 어노테이션이 적용된 todayProductRank 메서드의 캐시 동작 검증
 */
@SpringBootTest
@ActiveProfiles("test")
public class ProductFacadeCacheTest {
    
    @Autowired
    private ProductFacade productFacade;
    
    @MockBean
    private ProductRankService productRankService;
    
    @Test
    @DisplayName("todayProductRank 메서드 캐시 적용 테스트")
    public void testTodayProductRankCaching() {
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
    
    @Test
    @DisplayName("다른 키로 조회 시 캐시 미스 테스트")
    public void testCacheMissWithDifferentKey() {
        // 실제로는 다른 키를 사용할 수 없지만, 캐시가 어떻게 작동하는지 이해하기 위한 참고 테스트
        // 실제 프로덕션 환경에서는 캐시 키가 'today'로 고정되어 있음
        
        // 이 테스트는 개념적 설명용이며, 실제로는 @CacheEvict를 사용하여 캐시를 명시적으로 제거해야 함
        
        // given: mock 데이터 준비
        List<DomainProductRank> mockRanks = new ArrayList<>();
        when(productRankService.todayProductRank()).thenReturn(mockRanks);
        
        // when: 메서드 호출
        List<ProductRankCommand> result = productFacade.todayProductRank();
        
        // then: 결과 확인 및 서비스 호출 확인
        assertEquals(0, result.size()); // 빈 목록이 반환되어야 함
        verify(productRankService, times(1)).todayProductRank();
    }
}