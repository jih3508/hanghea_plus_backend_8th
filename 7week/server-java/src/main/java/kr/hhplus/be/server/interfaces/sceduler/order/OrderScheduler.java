package kr.hhplus.be.server.interfaces.sceduler.order;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.application.order.OrderFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final OrderFacade facade;


    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void makeRank(){
        facade.updateRank();
    }
}
