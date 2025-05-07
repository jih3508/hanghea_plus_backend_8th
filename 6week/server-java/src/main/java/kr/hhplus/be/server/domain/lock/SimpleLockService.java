package kr.hhplus.be.server.domain.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleLockService implements LockService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public <T> T executeWithLock(String key, Long waitTime, Long leaseTime, TimeUnit timeUnit ,Supplier<T> supplier) {

        boolean acquired = false;
        try {
            log.debug("락 획득 시도: {}", key);
            // SETNX 명령어로 락 획득 시도 (값이 있으면 실패, 없으면 성공)
            acquired = lock(key, leaseTime, timeUnit);

            if (!acquired) {
                throw new IllegalStateException("Failed to acquire Simple lock for key: " + key);
            }

            return supplier.get();
        }finally{
            // 락 해제
            if (acquired) {
                unLock(key);
            }
        }

    }

    @Override
    public <T> T executeWithLockList(List<String> keys, Long waitTime, Long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {
        // 데드락 방지를 위해 키 정렬
        keys.sort(String.CASE_INSENSITIVE_ORDER);

        // 획득한 락을 추적하기 위한 리스트
        List<String> acquiredLocks = new LinkedList<>();

        try {
            // 모든 락 획득 시도
            for (String key : keys) {
                log.debug("락 획득 시도: {}", key);
                boolean acquired = lock(key, leaseTime, timeUnit);

                if (!acquired) {
                    // 락 획득 실패 시 이미 획득한 모든 락 해제
                    releaseAllLocks(acquiredLocks);
                    throw new IllegalStateException("Failed to acquire Simple lock for key: " + key);
                }

                // 성공적으로 획득한 락 추적
                acquiredLocks.add(key);
            }

            // 모든 락을 획득한 상태에서 supplier 실행
            return supplier.get();
        } finally {
            // 모든 락 해제
            releaseAllLocks(acquiredLocks);
        }
    }

    // 획득한 모든 락을 해제하는 헬퍼 메소드
    private void releaseAllLocks(List<String> acquiredLocks) {
        // 획득한 순서의 역순으로 락 해제 (스택 방식)
        for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
            String key = acquiredLocks.get(i);
            try {
                unLock(key);
                log.debug("락 해제 완료: {}", key);
            } catch (Exception e) {
                log.error("락 해제 중 오류 발생: {}", key, e);
            }
        }
    }

    private boolean lock(String key, Long leaseTime, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(Thread.currentThread().getId()), leaseTime, timeUnit));
    }

    private void unLock(String key){
        redisTemplate.delete(key);
    }
}
