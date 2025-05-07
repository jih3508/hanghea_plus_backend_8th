package kr.hhplus.be.server.common.aop;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.LockStrategy;
import kr.hhplus.be.server.common.lock.LockType;
import kr.hhplus.be.server.domain.lock.LockServiceTemplate;
import kr.hhplus.be.server.domain.product.service.ProductStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DistributedLockAspect {

    private final ApplicationContext applicationContext;

    private final Map<LockStrategy, String> lockBeanNames = new HashMap<>() {{
        put(LockStrategy.SIMPLE_LOCK, "simpleLockService");
        put(LockStrategy.SPIN_LOCK, "spinLockService");
        put(LockStrategy.PUB_SUB_LOCK, "pubSubLockService");
    }};

    private final ProductStockService stockService;

    @Around("@annotation(kr.hhplus.be.server.common.lock.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 어노테이션 정보 가져오기
        DistributedLock lockable = method.getAnnotation(DistributedLock.class);
        LockServiceTemplate lockService = getLockImplementation(lockable.strategy());
        LockType lockType = lockable.type();


        switch (lockType) {
            case PRODUCT ->{
                List<String> keys = Arrays.stream(lockable.keys())
                        .map(key ->  stockService.getStock(Long.parseLong(key)))
                        .map(stock -> lockType.getCode() + stock.getId())
                        .toList();

                return lockService.executeWithLockList(keys, lockable.waitTime(), lockable.leaseTime(), lockable.timeUnit() ,joinPoint::proceed);
            }
            default -> {
                String key = lockType.getCode() + lockable.key();
                return lockService.executeWithLock(key, lockable.waitTime(), lockable.leaseTime(), lockable.timeUnit() , joinPoint::proceed);
            }
        }


    }

    private LockServiceTemplate getLockImplementation(LockStrategy  lockStrategy) {
        return applicationContext.getBean(lockBeanNames.getOrDefault(lockStrategy, "simpleLockService"), LockServiceTemplate.class);
    }
}
