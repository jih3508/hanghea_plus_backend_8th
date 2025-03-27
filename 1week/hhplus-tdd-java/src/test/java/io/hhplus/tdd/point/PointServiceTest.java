package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.impl.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointValidation validation;

    private Map<Long, Long> userInfo;

    @BeforeEach
    void userSet(){
        userInfo = new HashMap<>();
        userInfo.put(1L, 1000l);
        userInfo.put(2L, 2000l);
        userInfo.put(3L, 3000l);

//        userPointTable.insertOrUpdate(1, userInfo.get(1L));
//        userPointTable.insertOrUpdate(2, userInfo.get(2L));
//        userPointTable.insertOrUpdate(3, userInfo.get(3L));

    }


    @Test
    @DisplayName("유저 포인트 조회 했을때 테스트")
    void 유저_포인트_조회_테스트() {

        // given
        Long id = 1L;
        long amount = 1000L;

        // when 1번 유저 조회
        when(pointService.point(id)).thenReturn(new UserPoint(id, amount, System.currentTimeMillis()));
        UserPoint userPoint = pointService.point(id);


        // 1번 유저 조회 됬는지 확인
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(1000L);
    }


    @Test
    @DisplayName("유저 포인트 충전 했을때 정상적으로 충전 됬는지 확인")
    void 유저_포인트_정상적인_충전_테스트(){
        // given
        Long id = 1L;
        long amount = 5000L;
        UserPoint result = new UserPoint(id, amount + userInfo.get(id), System.currentTimeMillis());

        // when
        when(userPointTable.selectById(id)).thenReturn(new UserPoint(id, userInfo.get(id), System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(id, 6000L)).thenReturn(result);
        //when(pointService.charge(id, amount)).thenReturn(result);
        UserPoint userPoint = pointService.charge(id, amount);

        //System.out.println(userPoint);
        //then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(6000L);
        verify(userPointTable, times(1)).insertOrUpdate(eq(id), eq(6000L));
        verify(pointHistoryTable, times(1)).insert(eq(id), eq(amount), eq(TransactionType.CHARGE), anyLong());

    }

    @Test
    @DisplayName("유저 포인트 충전 했을때 정상적으로 안되었을때(충전 포인트 최소 미만 일때)")
    void 유저_포인트_충전_실패_테스트1(){
        // given
        Long id = 2L;
        long amount = -100l;
        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());

        //when
        when(userPointTable.selectById(id)).thenReturn(userPoint);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("최소 %d이상 충전 해주세요", PointVariable.MIN_CHARGE_POINT)))
                .when(validation).userPointIsUsed(userPoint, amount);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pointService.charge(id, amount);
        });

        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), eq(TransactionType.CHARGE), anyLong());

    }

    @Test
    @DisplayName("유저 포인트 충전 했을때 정상적으로 안되었을때(충전 후 포인트가 초과가 되었을때)")
    void 유저_포인트_충전_실패_테스트2(){
        // given
        Long id = 3L;
        long amount = PointVariable.MAX_POINT - 2000l;
        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());

        //when
        when(userPointTable.selectById(id)).thenReturn(userPoint);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("최대 충전 할수 있는 포인트(%d) 초과 했습니다.", PointVariable.MAX_POINT)))
                .when(validation).userPointIsUsed(userPoint, amount);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pointService.charge(id, amount);
        });

        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), eq(TransactionType.CHARGE), anyLong());

    }

    @Test
    @DisplayName("유저 포인트 사용했을때 정상적으로 동작하는지 테스트")
    void 유저_포인트_사용_정상_테스트(){
        // given
        Long id = 1L;
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(id, 10000l, System.currentTimeMillis());
        UserPoint result = new UserPoint(id, 10000l-amount, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(id)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(id, 5000L)).thenReturn(result);
        userPoint = pointService.use(id, amount);

        //System.out.println(userPoint);
        //then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(5000L);
        verify(userPointTable, times(1)).insertOrUpdate(eq(id), eq(5000L));
        verify(pointHistoryTable, times(1)).insert(eq(id), eq(amount), eq(TransactionType.USE), anyLong());

    }

    @Test
    @DisplayName("유저 포인트 사용했을때 한도 초과일때 테스트")
    void 유저_포인트_사용_실패_테스트(){
        // given
        Long id = 1L;
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(id, 1000l, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(id)).thenReturn(userPoint);
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,String.format("한도 초과가 되었습니다.", PointVariable.MAX_POINT)))
                .when(validation).userPointIsUsed(userPoint, amount);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pointService.use(id, amount);
        });

        //System.out.println(userPoint);
        //then

        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), eq(TransactionType.USE), anyLong());

    }

    @Test
    @DisplayName("유저 포인트 내역 조회 테스트")
    void 포인트_조회_테스트(){
        // given
        List<PointHistory> expectedHistory = List.of(
                new PointHistory(1,1, 10000l, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, 1,5000l, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(3, 1,3000l, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(1)).thenReturn(expectedHistory);
        List<PointHistory> result = pointService.history(1l);


        assertThat(result).isEqualTo(expectedHistory);
        verify(pointHistoryTable, times(1)).selectAllByUserId(1l);
    }



}