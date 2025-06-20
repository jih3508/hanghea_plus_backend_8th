package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.service.UserService;
import kr.hhplus.be.server.infrastructure.point.entity.Point;
import kr.hhplus.be.server.domain.point.service.PointHistoryService;
import kr.hhplus.be.server.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PointFacade {

    private final PointService service;
    private final PointHistoryService historyService;


    private final UserService userService;

    @Transactional(readOnly = true)
    public BigDecimal getPoint(Long userId) {

        userService.findById(userId);
        DomainPoint point = service.getPoint(userId);

        return point.getPoint();
    }

    /*
     * method: charge
     * description: 포인트 충전
     */
    @Transactional(rollbackFor =  Exception.class)
    public BigDecimal charge(PointChargeCommand command) {

        userService.findById(command.getUserID());
        DomainPoint point = service.charge(command.getUserID(), command.getAmount());

        historyService.chargeHistory(command.getUserID(), command.getAmount());

        return point.getPoint();
    }

}
