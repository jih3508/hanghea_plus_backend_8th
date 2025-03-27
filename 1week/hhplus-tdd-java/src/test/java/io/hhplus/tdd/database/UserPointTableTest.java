package io.hhplus.tdd.database;

import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserPointTableTest {

    @Mock
    private UserPointTable userPointTable;

    private Map<Long, Long> userInfo;

    @BeforeEach
    void basicDataset() {
        userInfo = new HashMap<>();
        userInfo.put(1L, 1000l);
        userInfo.put(2L, 2000l);
        userInfo.put(3L, 3000l);

        userPointTable.insertOrUpdate(1, userInfo.get(1L));
        userPointTable.insertOrUpdate(2, userInfo.get(2L));
        userPointTable.insertOrUpdate(3, userInfo.get(3L));
    }

    @Test
    @DisplayName("사용자 포인트 등록 한것 테스트")
    void 사용자_포인트_등록_테스트(){

        // given
        long userId = 4;
        long amount = 40000;

        // when
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));
        userPointTable.insertOrUpdate(userId, amount);


        // then
        UserPoint userPoint = userPointTable.selectById(4l);


        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(amount);
    }

    @Test
    @DisplayName("등록된 사용자 포인트 테스트")
    void 사용자_포인트_조회_테스트(){
        //given
        long userId = 1l;

        // when
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, userInfo.get(userId), System.currentTimeMillis()));
        UserPoint userPoint = userPointTable.selectById(userId);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.point()).isEqualTo(userInfo.get(userId));
    }


}