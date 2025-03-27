package io.hhplus.tdd.point.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;
    private final PointValidation validation;

    private final Map<Long, Long> recentAccess = new ConcurrentHashMap<>();

    // 사용자별 락 관리 (동일 ID 요청 시 순차 실행)
    private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    /*
     *
     * description 제한 시간내에 같은 유저 사용 하는지 확인 하는 함수
     */
    private void isUserRecent(long userId) {

        long currentTime = System.currentTimeMillis();
        // 제한 시간내에 사용한 적이 있으면 오류 보냄
        if(recentAccess.get(userId) != null && (currentTime - recentAccess.get(userId) <= PointVariable.USER_LOCK_TIME )){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "사용자 포인트 이용 중입니다.");
        }
    }

    private ReentrantLock getLock(long id) {
        return lockMap.computeIfAbsent(id, k -> new ReentrantLock());
    }

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

        ReentrantLock lock = getLock(id);
        if (!lock.tryLock()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "동일한 사용자에 대한 요청이 처리 중입니다.");
        }
        try {
            lock.lock();
            isUserRecent(id);

            UserPoint userPoint = userPointTable.selectById(id);
            // 유저 포인트 사용 가능한지 확인
            validation.isChargePoint(userPoint, amount);

            // 포인트 중전
            userPoint = userPointTable.insertOrUpdate(id,userPoint.point() + amount);

            // 포인트 충전 내역
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());

            recentAccess.put(id, System.currentTimeMillis());
            return userPoint;
        }finally {
            lock.unlock();
        }

    }

    /*
     * description: 특정 유저의 포인트를 사용하는 기능
     */
    @Override
    public UserPoint use(long id, long amount) {

        ReentrantLock lock = getLock(id);
        if (!lock.tryLock()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "동일한 사용자에 대한 요청이 처리 중입니다.");
        }try {
            lock.lock();
            isUserRecent(id);

            UserPoint userPoint = userPointTable.selectById(id);
            // 유저 포인트 사용 가능한지 확인
            validation.userPointIsUsed(userPoint, amount);
            userPoint  = userPointTable.insertOrUpdate(id, userPoint.point() - amount);

            // 포인트 충전 내역
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());


            recentAccess.put(id, System.currentTimeMillis());
            return userPoint;
        }finally {
            lock.unlock();
        }

    }
}
