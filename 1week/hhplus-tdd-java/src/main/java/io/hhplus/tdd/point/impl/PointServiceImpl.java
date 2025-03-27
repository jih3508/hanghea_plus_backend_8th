package io.hhplus.tdd.point.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;
    private final PointValidation validation;

    /*
     * description: 특정 유저의 포인트를 조회하는 기능
     */
    @Override
    public UserPoint point(long id) {
        return userPointTable.selectById(id);
    }

    /*
     * description: 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     */
    @Override
    public List<PointHistory> history(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    /*
     * description: 특정 유저의 포인트를 충전하는 기능
     */
    @Override
    public UserPoint charge(long id, long amount) {

        UserPoint userPoint = userPointTable.selectById(id);
        // 유저 포인트 사용 가능한지 확인
        validation.userPointIsUsed(userPoint, amount);

        // 포인트 중전
        userPoint = userPointTable.insertOrUpdate(id,userPoint.point() + amount);

        // 포인트 충전 내역
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

        return userPoint;
    }

    /*
     * description: 특정 유저의 포인트를 사용하는 기능
     */
    @Override
    public UserPoint use(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        // 유저 포인트 사용 가능한지 확인
        validation.userPointIsUsed(userPoint, amount);
        userPoint  = userPointTable.insertOrUpdate(id, userPoint.point() - amount);

        // 포인트 충전 내역
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());

        return userPoint;
    }
}
