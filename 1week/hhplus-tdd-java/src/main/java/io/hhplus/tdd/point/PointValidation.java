package io.hhplus.tdd.point;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PointValidation {


    /*
     * method: isChargePoint
     * description: 포인트 충전 가능한지 여부
     */

    public void isChargePoint (UserPoint userPoint, long amount) {

        // 최소 충전 가능한지 여부
        if (amount < PointVariable.MIN_CHARGE_POINT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("최소 %d이상 충전 해주세요", PointVariable.MIN_CHARGE_POINT));
        }


        // 최대 보관 할수 있는 충전 포인트가 초과 될 경우
        if((userPoint.point() + amount) > PointVariable.MAX_POINT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("최대 충전 할수 있는 포인트(%d) 초과 했습니다.", PointVariable.MAX_POINT));
        }

    }

    /*
     * method: userPointIsUsed
     * description: 포인트 사용 가능한지 여부 체크
     */
    public void userPointIsUsed(UserPoint userPoint, long amount) {
        if (userPoint.point() < amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "한도 초과가 되었습니다.");
        }

    }
}
