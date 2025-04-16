package kr.hhplus.be.server.application.point;

import kr.hhplus.be.server.domain.user.service.UserService;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import kr.hhplus.be.server.domain.point.entity.Point;
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

    public BigDecimal getPoint(Long userId) {

        userService.findById(userId);
        Point point = service.getPoint(userId);

        return point.getPoint();
    }

    /*
     * method: charge
     * description: 포인트 충전
     */
    @Transactional
    public BigDecimal charge(PointChargeCommand command) {

        User user = userService.findById(command.getUserID());
        Point point = service.charge(command.getUserID(), command.getAmount());

        historyService.chargeHistory(user, command.getAmount());

        return point.getPoint();
    }



}
