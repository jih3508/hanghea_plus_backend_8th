package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.UpdatePoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("포인트 서비스 통합 테스트")
class PointServiceIntegrationTest extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceIntegrationTest.class);

    @Autowired
    private PointService service;

    @Autowired
    private PointRepository repository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        CreateUser user = CreateUser.builder()
                .name("홍길동")
                .id("test1")
                .build();

        User resultUser = userRepository.create(user);
        repository.create(resultUser.getId());

        user = CreateUser.builder()
                .name("강감찬")
                .id("test2")
                .build();

        resultUser = userRepository.create(user);
        DomainPoint point = repository.create(resultUser.getId());
        repository.update(UpdatePoint.builder()
                        .pointId(point.getId())
                        .point(new BigDecimal(1_000_000_000L))
                .build());


        user = CreateUser.builder()
                .name("이순신")
                .id("test3")
                .build();

        resultUser = userRepository.create(user);
        point = repository.create(resultUser.getId());
        repository.update(UpdatePoint.builder()
                .pointId(point.getId())
                .point(new BigDecimal(1_000_000L))
                .build());
    }

    @Test
    @DisplayName("사용자 없는 경우 포인트도 없고 오류를 보낸다.")
    void 없는_포인트_조회(){
        //given
        Long userId= 10L;

        // when && that
        assertThatThrownBy(() -> service.getPoint(userId))
        .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 있는 경우 포인트를 조히한다.")
    void 있는_포인트_조회(){
        // given
        Long userId = 1L;

        // when
        DomainPoint point = service.getPoint(userId);

        assertThat(point).isNotNull();
        assertThat(point.getUserId()).isEqualTo(userId);
        assertThat(point.getPoint()).isEqualTo(BigDecimal.ZERO);

    }

    @DisplayName("충전 통테스트")
    @Nested
    class 충전{

        @Test
        @DisplayName("충전 후 금액이 한도 초과 일때")
        void 충전_금액_한도_초과(){
            // given
            Long userId = 2L;
            BigDecimal amount = new BigDecimal(1_000_000_000L);

            // when && that
            assertThatThrownBy(() -> service.charge(userId, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("충전후 포인트가 한도 초과 되었습니다.");

        }

        @Test
        @DisplayName("충금 금액이 0원 이하일때 실패!!")
        void 충전_금액0(){
            // given
            Long userId = 2L;
            BigDecimal amount = new BigDecimal(-1_000_000_000L);

            // when && then
            assertThatThrownBy(() -> service.charge(userId, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("충전 포인트 금액이 1원 이상이여야 합니다.");
        }

        @Test
        @DisplayName("정상 적으로 충전이 되었을때")
        void 충전_테스트(){
            // given
            Long userId = 1L;
            BigDecimal amount = new BigDecimal(1_000_000_000L);
            DomainPoint point = service.getPoint(userId);


            // when
            DomainPoint result = service.charge(userId, amount);

            assertThat(result).isNotNull();
            assertThat(result.getPoint()).isEqualTo(point.getPoint().add(amount));

        }

    }

    @Nested
    @DisplayName("사용 테스트")
    class 사용{

        @Test
        @DisplayName("잔액 부족으로 충전 실패")
        void 잔액_부족(){
            // given
            Long userId = 1L;
            BigDecimal amount = new BigDecimal(100_000L);

            //when && then
            assertThatThrownBy(() -> service.use(userId, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("잔액 부족이 부족합니다. 충전후 결제 요청 드립니다.");
        }

        @Test
        @DisplayName("정상적으로 사용 처리가 되었을때")
        void 사용_처리(){
            // given
            Long userId = 3L;
            BigDecimal amount = new BigDecimal(100_000L);
            DomainPoint point = service.getPoint(userId);

            // when
            DomainPoint result = service.use(userId, amount);

            assertThat(result).isNotNull();
            assertThat(result.getPoint()).isEqualTo(point.getPoint().subtract(amount));

        }
    }
}