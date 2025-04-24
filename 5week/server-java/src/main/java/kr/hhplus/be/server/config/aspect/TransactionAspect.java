package kr.hhplus.be.server.config.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 애플리케이션 레이어의 메서드에 트랜잭션 관리 기능을 제공하는 관점(Aspect)
 * @Transactional 어노테이션이 없는 메서드에 대해 트랜잭션을 적용
 */
@Slf4j
@Aspect
@Component
public class TransactionAspect {

    private final PlatformTransactionManager transactionManager;

    public TransactionAspect(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 애플리케이션 레이어의 모든 메서드 중 @Transactional 어노테이션이 없는 메서드에 대한 포인트컷
     */
    @Pointcut("execution(* kr.hhplus.be.server.application.*.*.*(..)) && !@annotation(org.springframework.transaction.annotation.Transactional)")
    public void applicationLayerPointcut() {
        // 애플리케이션 레이어의 모든 메서드 중 @Transactional 어노테이션이 없는 메서드
    }

    /**
     * 트랜잭션 관리 로직 적용
     */
    @Around("applicationLayerPointcut()")
    public Object aroundApplicationLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.debug("{}. {} 메서드에 대한 트랜잭션 시작", className, methodName);

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            Object result = joinPoint.proceed();
            transactionManager.commit(status);
            log.debug("{}. {} 메서드에 대한 트랜잭션 커밋 완료", className, methodName);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            log.error("{}. {} 메서드 실행 중 오류로 인한 트랜잭션 롤백: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}