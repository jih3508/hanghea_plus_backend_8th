package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.model.DomainPointHistory;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.point.entity.PointTransactionType;
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

@DisplayName("포인트 이력 서비스 통합 테스트")
class PointHistoryServiceIntegrationTest  extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointHistoryServiceIntegrationTest.class);

    @Autowired
    private PointHistoryService service;

    @Autowired
    private PointHistoryRepository repository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        CreateUser user = CreateUser.builder()
                .name("홍길동")
                .id("test1")
                .build();

        userRepository.save(user);
    }

    @Test
    @DisplayName("포인트 이력 충전 테스트")
    void 충전_이력(){
        //given
        Long userId = 1L;
        BigDecimal  amount = new BigDecimal("10000");

        // when
        service.chargeHistory(userId, amount);
        DomainPointHistory result = repository.findByAtLast().get();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getType()).isEqualTo(PointTransactionType.CHARGE);

    }

    @Test
    @DisplayName("포인트 이력 사용 테스트")
    void 사용_이력(){
        //given
        Long userId = 1L;
        BigDecimal  amount = new BigDecimal("10000");

        // when
        service.useHistory(userId, amount);
        DomainPointHistory result = repository.findByAtLast().get();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getType()).isEqualTo(PointTransactionType.USE);
    }


}