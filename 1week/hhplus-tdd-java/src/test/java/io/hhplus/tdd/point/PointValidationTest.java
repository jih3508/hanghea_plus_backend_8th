package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PointValidationTest {

    @InjectMocks
    private PointValidation validation;

    @Test
    @DisplayName("포인트 정상적으로 충전 했을때 아무런 일이 일어 나지 않는다.")
    void 포인트_정상_충전(){
        // given
        long amount = 3000;
        UserPoint userPoint = new UserPoint(1l, 3000l,  System.currentTimeMillis());

        // when, then
        assertDoesNotThrow(() -> validation.isChargePoint(userPoint, amount));
    }


    @Test
    @DisplayName("포인트 최소로 충전 했을때 오류가 난다.")
    void 최소_미만_충전_했을때(){

        // given
        long amount = 500l;
        UserPoint userPoint = new UserPoint(1l, 3000l,  System.currentTimeMillis());

        // when, then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                validation.isChargePoint(userPoint, amount));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo(String.format("최소 %d이상 충전 해주세요", PointVariable.MIN_CHARGE_POINT));

    }

    @Test
    @DisplayName("포인트 충전후 한도가 초과 되었을때 초과 에러가 나야 한다.")
    void 충저_했을때_최대_충전_포인트_초과(){

        // given
        long amount = 2000l;
        UserPoint userPoint = new UserPoint(1l, PointVariable.MAX_POINT - 1000l,  System.currentTimeMillis());

        // when, then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                validation.isChargePoint(userPoint, amount));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo(String.format("최대 충전 할수 있는 포인트(%d) 초과 했습니다.", PointVariable.MAX_POINT));

    }

    @Test
    @DisplayName("포인트 사용 했을때 정상적적일때")
    void 정상적인_사용_가능(){
        // given
        long amount = 2000l;
        UserPoint userPoint = new UserPoint(1l, 3000l,  System.currentTimeMillis());

        // when, then
        assertDoesNotThrow(() -> validation.userPointIsUsed(userPoint, amount));
    }


    @Test
    @DisplayName("포인트 사용 했을때 한도가 초과 되었을때")
    void 정상적인_사용_가능하지_못할때(){
        // given
        long amount = 5000l;
        UserPoint userPoint = new UserPoint(1l, 3000l,  System.currentTimeMillis());

        // when, then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                validation.userPointIsUsed(userPoint, amount));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getReason()).isEqualTo("한도 초과가 되었습니다.");
    }
}