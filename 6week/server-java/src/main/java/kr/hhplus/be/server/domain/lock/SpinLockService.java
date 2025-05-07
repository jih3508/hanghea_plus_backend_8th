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
public class SpinLockService implements LockServiceTemplate {


    private final RedisTemplate<String, String> redisTemplate;
    private static final long RETRY_DELAY_MS = 100L;
    private static final int MAX_RETRIES = 30;

    @Override
    public <T> T executeWithLock(String key, Long waitTime, Long leaseTime, TimeUnit timeUnit, LockCallback<T> callback) throws Throwable{
        boolean acquired = false;
        int retryCount = 0;

        try {
            log.debug("락 획득 시도: {}", key);
            while (!acquired ) {
                acquired = lock(key, leaseTime, timeUnit);

                if (!acquired) {
                    ++retryCount;
                    log.debug("Waiting for spin lock, key: {}, attempt: {}", key, retryCount);
                    if(retryCount > MAX_RETRIES){
                        throw new IllegalStateException("락 획득 시도 횟수 초과: " + key);
                    }

                }

                // 재시도 전 짧은 대기 (CPU 부하 방지)
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("락 획득 중 인터럽트 발생: " + key, e);
                }

            }
            // 락 획득 성공 시 로직 실행
            return  callback.doInLock();
        }  finally {
            // 락 해제
            if (acquired) {
                unLock(key);
            }
        }

    }

    @Override
    public <T> T executeWithLockList(List<String> keys, Long waitTime, Long leaseTime, TimeUnit timeUnit, LockCallback<T> callback) throws Throwable{
        // 데드락 방지를 위해 키 정렬
        keys.sort(String.CASE_INSENSITIVE_ORDER);

        // 획득한 락을 추적하기 위한 리스트
        List<String> acquiredLocks = new LinkedList<>();

        try {
            // 모든 락 획득 시도
            for (String key : keys) {
                log.debug("락 획득 시도: {}", key);
                Boolean acquired = false;
                int retryCount = 0;
                while (!acquired) {
                    acquired = lock(key, leaseTime, timeUnit);
                    if (!acquired) {
                        ++retryCount;
                        log.debug("Waiting for spin lock, key: {}, attempt: {}", key, retryCount);
                        if(retryCount > MAX_RETRIES){
                            // 락 획득 실패 시 이미 획득한 모든 락 해제
                            releaseAllLocks(acquiredLocks);
                            throw new IllegalStateException("락 획득 시도 횟수 초과: " + key);
                        }

                    }

                    // 재시도 전 짧은 대기 (CPU 부하 방지)
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        releaseAllLocks(acquiredLocks);
                        throw new IllegalStateException("락 획득 중 인터럽트 발생: " + key, e);
                    }

                    // 성공적으로 획득한 락 추적
                    acquiredLocks.add(key);
                }

            }

            // 모든 락을 획득한 상태에서 supplier 실행
            return callback.doInLock();
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
