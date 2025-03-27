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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceTest.class);

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

    @Test
    @DisplayName("충전시 같은 사용자가 30초 이내 이용 했을때 오류 나는지 확인")
    void 충전시_30초내_유저_동시_접속자_테스트(){
        Long userId = 1L;
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(userId, 10000l, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 첫 번째 요청 성공
        pointService.charge(userId, amount);

        // 두 번째 요청 (30초 내)
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pointService.charge(userId, amount);
        });


        // then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo( "사용자 포인트 이용 중입니다.");

    }


    @Test
    @DisplayName("충전시 같은 사용자가 30초 이후에는 이용 했을때 정상으로 되는지 확인")
    void 충전시_30초이후_유저_동시_접속자_테스트() throws InterruptedException {
        Long userId = 1L;
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(userId, 10000l, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 첫 번째 요청 성공
        pointService.charge(userId, amount);

        TimeUnit.SECONDS.sleep(31);

        // 두 번째 요청 (30초 이후)
        userPoint = new UserPoint(userId, 10000l+amount, System.currentTimeMillis());
        UserPoint result = new UserPoint(userId, 10000l+amount * 2, System.currentTimeMillis());
        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(userId, 10000l+amount * 2)).thenReturn(result);
        UserPoint resultPoint = pointService.charge(userId, amount);

        assertThat(resultPoint).isNotNull();
        assertThat(resultPoint.point()).isEqualTo(10000l+amount * 2);

    }

    @Test
    @DisplayName("사용시 같은 사용자가 30초 이내 이용 했을때 오류 나는지 확인")
    void 사용시_유저_동시_접속자_테스트(){
        Long userId = 1L;
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(userId, 10000l, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 첫 번째 요청 성공
        pointService.charge(userId, amount);

        // 두 번째 요청 (30초 내)
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pointService.use(userId, amount);
        });


        // then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo( "사용자 포인트 이용 중입니다.");


    }


    @Test
    @DisplayName("사용시 같은 사용자가 30초 이후에는 이용 했을때 정상으로 되는지 확인")
    void 사용시_30초이후_유저_동시_접속자_테스트() throws InterruptedException {
        Long userId = 1L;
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(userId, 12000l, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 첫 번째 요청 성공
        pointService.use(userId, amount);

        TimeUnit.SECONDS.sleep(31);

        // 두 번째 요청 (30초 이후)
        userPoint = new UserPoint(userId, 12000l-amount, System.currentTimeMillis());
        UserPoint result = new UserPoint(userId, 12000l-amount * 2, System.currentTimeMillis());
        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(userId, 12000l-amount * 2)).thenReturn(result);
        UserPoint resultPoint = pointService.use(userId, amount);

        assertThat(resultPoint).isNotNull();
        assertThat(resultPoint.point()).isEqualTo(12000l-amount * 2);

    }


    @Test
    @DisplayName("같은 사용자가 충전과 사용 동시에 30초 이내에는 이용 했을때 오류 나는지 확인")
    void 충전_사용_30초이내_유저_동시_접속자_테스트() throws InterruptedException {
        Long userId = 1L;
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(userId, 10000l, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 첫 번째 요청 성공
        pointService.charge(userId, amount);

        // 두 번째 요청 (30초 내)
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pointService.use(userId, amount);
        });

        // then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo( "사용자 포인트 이용 중입니다.");

    }

    @Test
    @DisplayName("같은 사용자가 충전과 사용 30초 이후에는 이용 했을때 정상으로 되는지 확인")
    void 사용_충전_30초이후_유저_동시_접속자_테스트() throws InterruptedException {
        long userId = 1L;
        long chargePoint = 5000l;
        UserPoint userPoint = new UserPoint(userId, 10000l, System.currentTimeMillis());

        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 첫 번째 요청 성공
        pointService.use(userId, chargePoint);

        TimeUnit.SECONDS.sleep(31);

        long usePoint = 8000l;

        // 두 번째 요청 (30초 이후)
        userPoint = new UserPoint(userId, 10000l+chargePoint, System.currentTimeMillis());
        UserPoint result = new UserPoint(userId, 10000l+ chargePoint - usePoint, System.currentTimeMillis());
        // when
        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(userId, 10000l+ chargePoint - usePoint)).thenReturn(result);
        UserPoint resultPoint = pointService.use(userId, usePoint);

        // then
        assertThat(resultPoint).isNotNull();
        assertThat(resultPoint.point()).isEqualTo(10000l+ chargePoint - usePoint);

    }

    @Test
    @DisplayName("충전시 동시에 요청 테스트")
    void 충전_동시_테스트() throws ExecutionException, InterruptedException {
        // given
        long userId = 1L;
        long amount = 5000l;
        UserPoint result = new UserPoint(userId, amount + userInfo.get(userId), System.currentTimeMillis());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when, then
        // 첫 번째 호출 (성공)
        // when
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, userInfo.get(userId), System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, 6000L)).thenReturn(result);
        Future<UserPoint> firstAttempt = executor.submit(() -> pointService.charge(userId, amount));
        //then
        assertThat(firstAttempt.get()).isNotNull();

        // 동시 요청 (바로 실행, 실패해야 함)
        Future<ResponseStatusException> secondAttempt = executor.submit(() -> {
            try {
                pointService.charge(userId, amount);
                return null;
            } catch (ResponseStatusException e) {
                return e;
            }
        });


        ResponseStatusException exception = secondAttempt.get();
        log.info(exception.toString());
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exception.getReason()).isEqualTo("동일한 사용자에 대한 요청이 처리 중입니다.");

    }

    @Test
    @DisplayName("사용시 동시에 요청 테스트")
    void 사용_동시_테스트() throws ExecutionException, InterruptedException {
        // given
        long userId = 1L;
        long amount = 5000l;
        UserPoint result = new UserPoint(userId, 10000l, System.currentTimeMillis());
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when, then
        // 첫 번째 호출 (성공)
        // when
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 10000l, System.currentTimeMillis()));
        when(userPointTable.insertOrUpdate(userId, 5000L)).thenReturn(result);
        Future<UserPoint> firstAttempt = executor.submit(() -> pointService.use(userId, amount));
        //then
        assertThat(firstAttempt.get()).isNotNull();

        // 동시 요청 (바로 실행, 실패해야 함)
        Future<ResponseStatusException> secondAttempt = executor.submit(() -> {
            try {
                pointService.use(userId, amount);
                return null;
            } catch (ResponseStatusException e) {
                return e;
            }
        });


        ResponseStatusException exception = secondAttempt.get();
        log.info(exception.toString());
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(exception.getReason()).isEqualTo("동일한 사용자에 대한 요청이 처리 중입니다.");

    }

}