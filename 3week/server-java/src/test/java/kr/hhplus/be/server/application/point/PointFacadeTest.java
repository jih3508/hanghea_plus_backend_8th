package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.UserService;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.point.entity.Point;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.domain.point.service.PointService;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointFacadeTest {

    private static final Logger log = LoggerFactory.getLogger(PointFacadeTest.class);

    final BigDecimal MAX_POINT = new BigDecimal(1_000_000_000L);

    @InjectMocks
    private PointFacade facade;

    @Mock
    private PointService service;

    @Mock
    private PointHistoryService historyService;

    @Mock
    private UserService userService;

    @Test
    @DisplayName("충전시 없는 유저일 경우 실패 한다.")
    void 충전_유저X(){


        //given
        PointChargeCommand command = new  PointChargeCommand(100L, BigDecimal.ZERO);

        // when
        when(userService.findById(anyLong())).thenThrow(new ApiExceptionResponse(HttpStatus.NOT_FOUND, "없는 사용자 입니다."));

        // then
        assertThatThrownBy(() -> facade.charge(command))
        .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("없는 사용자 입니다.");
        verify(service, never()).charge(anyLong(), any());
        verify(historyService, never()).chargeHistory(any(), any());
    }

    @Test
    @DisplayName("충전시 포인트 한도가 초과 되면 실패 한다.")
    void 충전_한도_초과(){
        //given
        PointChargeCommand command = new  PointChargeCommand(1L, MAX_POINT);
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("사용자1")
                .build();

        // when
        when(userService.findById(1L)).thenReturn(user);
        when(service.charge(1L, command.getAmount())).thenThrow(new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "충전후 포인트가 한도 초과 되었습니다."));

        assertThatThrownBy(() -> facade.charge(command))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("충전후 포인트가 한도 초과 되었습니다.");
        verify(userService, times(1)).findById(1L);
        verify(service, times(1)).charge(1L, command.getAmount());
        verify(historyService, never()).chargeHistory(any(), any());
    }

    @Test
    @DisplayName("정상적인 충전 테스트")
    void 충전_테스트(){

        //given
        PointChargeCommand command = new  PointChargeCommand(1L, new BigDecimal(1_000_000));
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("사용자1")
                .build();

        Point point = Point.builder()
                .id(1L)
                .user(user)
                .point(new BigDecimal(1_000_000))
                .build();

        // when
        when(userService.findById(1L)).thenReturn(user);
        when(service.charge(1L, command.getAmount())).thenReturn(point);

        BigDecimal result = facade.charge(command);

        assertThat(result).isEqualTo(new BigDecimal(1_000_000));
        verify(userService, times(1)).findById(1L);
        verify(service, times(1)).charge(1L, command.getAmount());
        verify(historyService, times(1)).chargeHistory(any(), any());

    }
}