package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.impl.PointServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PointServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceIntegrationTest.class);

    private PointService pointService;
    private PointHistoryTable pointHistoryTable;
    private UserPointTable userPointTable;
    private PointValidation validation;


    @BeforeEach
    void setUp() {
        // injection
        pointHistoryTable = new PointHistoryTable();
        userPointTable = new UserPointTable();
        validation = new PointValidation();
        pointService = new PointServiceImpl(pointHistoryTable,  userPointTable, validation);

        // basic user data set
        userPointTable.insertOrUpdate(1, 10000l);
        userPointTable.insertOrUpdate(2, 20000l);
        userPointTable.insertOrUpdate(3, 30000l);
    }

    @AfterEach
    void afterSetUp(){
        userPointTable.insertOrUpdate(1, 10000l);
        userPointTable.insertOrUpdate(2, 20000l);
        userPointTable.insertOrUpdate(3, 30000l);
    }


    @Test
    @DisplayName("유저 1,2,3번 테스트 해서 정상 적으로 나와야 하고 4번은 기존에 등록 되지 않아서 0원 나오면 된다.")
    void 유저_조회_테스트(){
        // given, when
        UserPoint user1 = pointService.point(1l);
        UserPoint user2 = pointService.point(2l);
        UserPoint user3 = pointService.point(3l);
        UserPoint user4 = pointService.point(4l);
        //then
        assertThat(user1).isNotNull();
        assertThat(user1.point()).isEqualTo(10000l);
        assertThat(user2).isNotNull();
        assertThat(user2.point()).isEqualTo(20000l);
        assertThat(user3).isNotNull();
        assertThat(user3.point()).isEqualTo(30000l);
        assertThat(user4).isNotNull();
        assertThat(user4.point()).isEqualTo(0l);
    }

    @Test
    @DisplayName("충전 -> 사용 -> 히스토리 정상적으로 나오는지 테스트")
    void 유저_충전후_사용_테스트() throws InterruptedException {
        // given
        long userId = 1l;
        long chargePoint = 10000l;
        long usePoint1 = 5000l;
        long usePoint2 = 10000l;

        // when
        pointService.charge(userId, chargePoint);

        TimeUnit.SECONDS.sleep(31l);

        pointService.use(userId, usePoint1);

        TimeUnit.SECONDS.sleep(31l);

        pointService.use(userId, usePoint2);

        UserPoint user1 = pointService.point(1l);

        List<PointHistory> user1History = pointHistoryTable.selectAllByUserId(userId);

        //then
        // 사용후 유저1은 5000포인트가 남아야 한다.
        assertThat(user1.point()).isEqualTo(5000l);

        //then
        // 이력이 3개가 쌓어야 한다.
        assertThat(user1History.size()).isEqualTo(3);
        // 충전 이력이 한개가 있어야 한다.
        assertThat(user1History.stream().filter(history-> history.type().equals(TransactionType.CHARGE)).toList().size())
                .isEqualTo(1);
        assertThat(user1History.stream().filter(history-> history.type().equals(TransactionType.USE)).toList().size())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("사용 했는데 한도가 초과 되었을때 사용 못하고 기존 포인트 변화가 없어야 한다.")
    void 유저_사용시_한도_초과(){
        // given
        long userId = 1l;
        long usePoint = 15000l;

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> pointService.use(userId, usePoint));
        UserPoint user1 = pointService.point(1l);
        List<PointHistory> user1History = pointHistoryTable.selectAllByUserId(userId);

        //then
        /*
         * 한도 초가 오류 나와야 함
         * history도 쌓으면 안된다.
         * 기존 포인트도 그대로 있어야 한다.
         */
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("한도 초과가 되었습니다.");
        assertThat(user1.point()).isEqualTo(10000l);
        assertThat(user1History.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("최소 미만 충전 + 충전후 한도가 넘을때")
    void 유저_포인트_충전_요건_만족_못할때(){
        // when
        long userId = 1l;
        long minChargePoint = 100l;
        long maxChargePoint = PointVariable.MAX_POINT * 2;

        // when
        // 최소 미만 충전시
        ResponseStatusException exception1 = assertThrows(ResponseStatusException.class, () -> pointService.charge(userId, minChargePoint));
        // 한도 충전 초과시
        ResponseStatusException exception2 = assertThrows(ResponseStatusException.class, () -> pointService.charge(userId, maxChargePoint));
        UserPoint user1 = pointService.point(1l);
        List<PointHistory> user1History = pointHistoryTable.selectAllByUserId(userId);


        //then
        /*
         * 최소충전 오류 + 한도 초과 오류가 나와야 함
         * history도 쌓으면 안된다.
         * 기존 포인트도 그대로 있어야 한다.
         */
        assertThat(exception1.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception2.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception1.getReason()).isEqualTo(String.format("최소 %d이상 충전 해주세요", PointVariable.MIN_CHARGE_POINT));
        assertThat(exception2.getReason()).isEqualTo(String.format("최대 충전 할수 있는 포인트(%d) 초과 했습니다.", PointVariable.MAX_POINT));
        assertThat(user1.point()).isEqualTo(10000l);
        assertThat(user1History.size()).isEqualTo(0);

    }

    @Test
    @DisplayName("동시에 여러개 충전 해도 처음에 충전 한것만 반영되어야 한다.")
    void 유저_포인트_동시에_충전_테스트() throws InterruptedException {
        // given
        long userId = 1l;
        long chargePoint = 2000l;
        int counter = 10;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(counter);

        List<Future<UserPoint>> futures = new LinkedList<>();
        List<Exception> exceptions = new LinkedList<>();

        // when
        for (int i = 0; i < counter; i++) {
            Future<UserPoint> future = executor.submit(() -> {
                try {
                    return pointService.charge(userId, chargePoint);
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                    return null;
                } finally {
                    countDownLatch.countDown();
                }
            });
            futures.add(future);
        }

        countDownLatch.await();

        UserPoint user1 = pointService.point(1l);
        List<PointHistory> user1History = pointHistoryTable.selectAllByUserId(userId);

        //then
        assertThat(user1.point()).isEqualTo(10000l+chargePoint);
        assertThat(user1History.size()).isEqualTo(1l);
        // 1개 빼고 나머지 오류가 나와야 한다.
        assertThat(exceptions.size()).isEqualTo(counter - 1);
    }


    @Test
    @DisplayName("동시에 여러개 사용 해도 처음에 사용 한것만 반영되어야 한다.")
    void 유저_포인트_동시에_사용_테스트() throws InterruptedException {
        // given
        long userId = 1l;
        long chargePoint = 2000l;
        int counter = 10;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(counter);

        List<Future<UserPoint>> futures = new LinkedList<>();
        List<Exception> exceptions = new LinkedList<>();

        // when
        for (int i = 0; i < counter; i++) {
            Future<UserPoint> future = executor.submit(() -> {
                try {
                    return pointService.use(userId, chargePoint);
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                    return null;
                } finally {
                    countDownLatch.countDown();
                }
            });
            futures.add(future);
        }

        countDownLatch.await();

        UserPoint user1 = pointService.point(1l);
        List<PointHistory> user1History = pointHistoryTable.selectAllByUserId(userId);

        //then
        assertThat(user1.point()).isEqualTo(10000l-chargePoint);
        assertThat(user1History.size()).isEqualTo(1l);
        // 1개 빼고 나머지 오류가 나와야 한다.
        assertThat(exceptions.size()).isEqualTo(counter - 1);
    }



}
