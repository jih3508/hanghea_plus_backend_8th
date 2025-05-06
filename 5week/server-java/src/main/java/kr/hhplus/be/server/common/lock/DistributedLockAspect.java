package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 분산락(Distributed Lock)을 처리하는 AOP 어드바이저
 * @DistributedLockable 어노테이션이 적용된 메서드에 대해 분산락을 적용
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private final ApplicationContext applicationContext;
    private final ExpressionParser parser = new SpelExpressionParser();
    
    private final Map<DistributedLockType, String> lockBeanNames = new HashMap<>() {{
        put(DistributedLockType.SIMPLE, "simpleLock");
        put(DistributedLockType.SPIN, "spinLock");
        put(DistributedLockType.REDISSON, "redissonLock");
    }};

    /**
     * @DistributedLockable 어노테이션이 적용된 메서드 실행 전후에 분산락 처리
     */
    @Around("@annotation(kr.hhplus.be.server.common.lock.DistributedLockable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 어노테이션 정보 가져오기
        DistributedLockable lockable = method.getAnnotation(DistributedLockable.class);
        String keyExpression = lockable.key();
        long timeoutMillis = lockable.timeoutMillis();
        DistributedLockType lockType = lockable.lockType();
        
        // SpEL을 이용한 키 표현식 평가
        String key = evaluateKeyExpression(keyExpression, joinPoint);
        
        // 락 타입에 맞는 구현체 가져오기
        DistributedLock lock = getLockImplementation(lockType);
        
        log.debug("Acquiring distributed lock: type={}, key={}", lockType, key);
        
        // 락 획득 후 메서드 실행
        return lock.executeWithLock(key, timeoutMillis, () -> {
            try {
                log.debug("Acquired lock: type={}, key={}", lockType, key);
                return joinPoint.proceed();
            } catch (Throwable t) {
                log.error("Error while executing locked method: type={}, key={}, error={}", lockType, key, t.getMessage(), t);
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new RuntimeException("Error in locked method execution", t);
                }
            } finally {
                log.debug("Released lock: type={}, key={}", lockType, key);
            }
        });
    }
    
    /**
     * SpEL을 이용해 키 표현식을 평가
     */
    private String evaluateKeyExpression(String keyExpression, ProceedingJoinPoint joinPoint) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            // 메서드 파라미터 정보 설정
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
            
            // 표현식 평가
            return parser.parseExpression(keyExpression).getValue(context, String.class);
        } catch (Exception e) {
            log.error("Error evaluating key expression: {}", keyExpression, e);
            // 표현식 평가 실패 시 고정 키와 타임스탬프 사용
            return "failed_key_" + System.currentTimeMillis();
        }
    }
    
    /**
     * 락 타입에 맞는 구현체 가져오기
     */
    private DistributedLock getLockImplementation(DistributedLockType lockType) {
        String beanName = lockBeanNames.getOrDefault(lockType, "redissonLock");
        return applicationContext.getBean(beanName, DistributedLock.class);
    }
}