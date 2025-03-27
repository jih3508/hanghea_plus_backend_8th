package io.hhplus.tdd.database;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PointHistoryTableTest {

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private UserPointTable userPointTable;

    @BeforeEach
    void basicDataset() {
        userPointTable.insertOrUpdate(1, 1000);
        userPointTable.insertOrUpdate(2, 2000);
        userPointTable.insertOrUpdate(3, 3000);
    }


    @Test
    @DisplayName("포인트 ")
    void 포인트_내역_충전_이력_등록_테스트(){
        // given
        UserPoint userPoint = userPointTable.selectById(1L);

        //when
        PointHistory pointHistory = pointHistoryTable.insert(userPoint.id(), 30000L, TransactionType.CHARGE, System.currentTimeMillis());

        // then
        assertThat(pointHistory.type()).isEqualTo(TransactionType.CHARGE);
        assertThat(pointHistory.amount()).isEqualTo(30000L);

    }

    @Test
    void 포인트_내역_사용_이력_등록_테스트(){
        // given
        UserPoint userPoint = userPointTable.selectById(1L);

        //when
        PointHistory pointHistory = pointHistoryTable.insert(userPoint.id(), 30000L, TransactionType.USE, System.currentTimeMillis());

        // then
        assertThat(pointHistory.type()).isEqualTo(TransactionType.USE);
        assertThat(pointHistory.amount()).isEqualTo(30000L);
    }


    @Test
    void 포인트_내역_이력_조회_테스트(){
        /*
         * 여러개 등록후 이력 내역 조회
         */
        // given
        UserPoint userPoint = userPointTable.selectById(1L);
        pointHistoryTable.insert(userPoint.id(), 30000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userPoint.id(), 10000L, TransactionType.USE, System.currentTimeMillis());
        pointHistoryTable.insert(userPoint.id(), 5000L, TransactionType.USE, System.currentTimeMillis());
        pointHistoryTable.insert(userPoint.id(), 50000L, TransactionType.CHARGE, System.currentTimeMillis());
        pointHistoryTable.insert(userPoint.id(), 20000L, TransactionType.USE, System.currentTimeMillis());

        //when
        List<PointHistory> pointHistories = pointHistoryTable.selectAllByUserId(1L);

        // then 등록한 것이 있는지 확인하기
        assertThat(pointHistories.size()).isNotEqualTo(0);
        pointHistories.stream().forEach(System.out::println);

    }


}