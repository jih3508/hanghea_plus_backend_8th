package kr.hhplus.be.server.common.event.order;

import kr.hhplus.be.server.domain.external.ExternalTransmissionService;
import kr.hhplus.be.server.domain.order.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCompleteEventListener {

    private final ExternalTransmissionService externalTransmissionService;

    @Async("asyncThreadPoolExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, value = OrderEvent.class)
    public void handler(OrderEvent event) {
        log.info("OrderCompleteEventListener called");

        boolean isSuccess = externalTransmissionService.sendOrderData(event.getOrder());
        if (isSuccess) {
            log.info("외부 데이터 전송 성공\n 외부 데이터: {}", event.getOrder());
        }else{
            log.error("외부 데이터 전송 실패\n 외부 데이터: {}", event.getOrder());
        }
    }

}
