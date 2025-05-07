package kr.hhplus.be.server.domain.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class PubSubLockService implements LockServiceTemplate {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String key, Long waitTime, Long leaseTime, TimeUnit timeUnit, LockCallback<T> callback) throws Throwable{

        RLock lock = redissonClient.getLock(key);
        Boolean acquired = false;
        try {
            log.debug("락 획득 시도: {}", key);
            acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            if(!acquired){
                throw new IllegalStateException("락 획득 실패: " + key);
            }

            return callback.doInLock();
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Lock acquisition interrupted for key: " + key, e);
        } finally {
            if (acquired) {
                log.debug("락 해제 시도: {}", key);
                lock.unlock();
            }
        }
    }

    @Override
    public <T> T executeWithLockList(List<String> keys, Long waitTime, Long leaseTime, TimeUnit timeUnit, LockCallback<T> callback) throws Throwable{


        // 데드락 방지를 위해 키 정렬
        keys.sort(String.CASE_INSENSITIVE_ORDER);

        // 모든 락 객체 생성
        RLock[] locks = new RLock[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            locks[i] = redissonClient.getLock(keys.get(i));
        }

        // MultiLock 생성
        RLock multiLock = redissonClient.getMultiLock(locks);
        boolean acquired = false;

        try {
            log.debug("다중 락 획득 시도: {}", keys);

            // 한 번의 호출로 모든 락 획득 시도
            acquired = multiLock.tryLock(waitTime, leaseTime, timeUnit);

            if (!acquired) {
                throw new IllegalStateException("다중 락 획득 실패: " + keys);
            }

            // 모든 락을 획득한 상태에서 supplier 실행
            return callback.doInLock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("다중 락 획득 중 인터럽트 발생: " + keys, e);
        } finally {
            if (acquired) {
                try {
                    log.debug("다중 락 해제 시도: {}", keys);
                    multiLock.unlock();
                } catch (Exception e) {
                    log.error("다중 락 해제 중 오류 발생: {}", keys, e);
                }
            }
        }
    }


}
