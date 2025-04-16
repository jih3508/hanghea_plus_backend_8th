package kr.hhplus.be.server.domain.user.point.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.point.service.PointService;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.domain.point.entity.Point;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);

    final BigDecimal MAX_POINT = new BigDecimal(1_000_000_000L);

    @InjectMocks
    private PointService service;

    @Mock
    private PointRepository repository;

    @Test
    @DisplayName("포인트 정보가 없은 경우 포인트 조회 살패 한다.")
    void 없는_포인트_조회(){
        // given, when
        when(repository.findByUserId(anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "포인트를 찾을 수 없습니다."));

        // then
        assertThatThrownBy(() -> service.getPoint(10L))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("포인트를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("포인트 정보가 없은 경우 포인트 정상적으로 조회")
    void 포인트_조회(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();

        Point expected = Point.builder()
                .id(1L)
                .user(user)
                .point(new BigDecimal(100_000))
                .build();

        // given, when
        when(repository.findByUserId(1L)).thenReturn(Optional.of(expected));

        Point point = service.getPoint(1L);

        assertThat(point).isEqualTo(expected);
    }


    @Test
    @DisplayName("충전시 포인트 조회시 없으면 실패한다.")
    void 충전_포인트_조회_실패(){
        // given, when
        when(repository.findByUserId(anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "포인트를 찾을 수 없습니다."));

        // then
        assertThatThrownBy(() -> service.charge(10L, any()))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("포인트를 찾을 수 없습니다.");
        verify(repository, never()).save(any());

    }

    @Test
    @DisplayName("충전시 초과 될 경우 실패한다.")
    void 충전_포인트_충전시_초과(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();

        Point point = Point.builder()
                .id(1L)
                .user(user)
                .point(MAX_POINT)
                .build();

        BigDecimal amount = new BigDecimal(100_000L);

        // when
        when(repository.findByUserId(1l)).thenReturn(Optional.of(point));

        // then
        assertThatThrownBy(() -> service.charge(1l, amount))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("충전후 포인트가 한도 초과 되었습니다.");

        verify(repository, never()).save(any());

    }


    @Test
    @DisplayName("포인트 충전 테스트")
    void 충전_포인트_충전(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();

        Point point = Point.builder()
                .id(1L)
                .user(user)
                .point(new BigDecimal(100_000))
                .build();

        BigDecimal amount = new BigDecimal(500_000L);

        Point expected = Point.builder()
                .id(1L)
                .user(user)
                .point(new BigDecimal(100_000).add(amount))
                .build();

        // when
        when(repository.findByUserId(1l)).thenReturn(Optional.of(point));
        when(repository.save(any(Point.class))).thenReturn(expected);

        Point result = service.charge(1l, amount);

        log.info("result={}", result);

        //then
        assertThat(result.getPoint()).isEqualTo(new BigDecimal(100_000).add(amount));
        verify(repository, times(1)).findByUserId(1L);
        verify(repository, times(1)).save(any(Point.class));

    }

    @Test
    @DisplayName("포인트 사용시 잔고가 부족할 경우 사용 못하게 한다.")
    public void 잔고_부족(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();

        Point point = Point.builder()
                .id(1L)
                .user(user)
                .point(new BigDecimal(100_000))
                .build();

        BigDecimal amount = new BigDecimal(500_000L);

        // when
        when(repository.findByUserId(1l)).thenReturn(Optional.of(point));

        assertThatThrownBy(() -> service.use(1l, amount))
        .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("잔액 부족!!!!");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("포인트 사용시 사용자 포인트 정상적으로 차감 된다.")
    public void 잔고_차감(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();

        Point point = Point.builder()
                .id(1L)
                .user(user)
                .point(new BigDecimal(100_000))
                .build();

        BigDecimal amount = new BigDecimal(50_000L);

        // when
        when(repository.findByUserId(1l)).thenReturn(Optional.of(point));
        when(repository.save(any(Point.class))).thenReturn(
                Point.builder()
                        .id(1L)
                        .user(user)
                        .point(new BigDecimal(50_000))
                        .build()
        );

        Point result = service.use(1l, amount);

        assertThat(result.getPoint()).isEqualTo(new BigDecimal(50_000L));
        verify(repository, times(1)).save(any(Point.class));
    }


}