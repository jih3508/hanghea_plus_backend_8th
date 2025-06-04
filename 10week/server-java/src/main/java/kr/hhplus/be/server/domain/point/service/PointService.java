package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.UpdatePoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository repository;

    /*
     * method: getPoint
     * description: 포인트 조회
     */

    public DomainPoint getPoint(Long userID) {

        return repository.findByUserIdLock(userID)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "포인트를 찾을 수 없습니다."));
    }

    /*
     * method: charge
     * description: 포인트 충전
     */
    public DomainPoint charge(Long userID, BigDecimal amount) {

        DomainPoint point = this.getPoint(userID);

        point.charge(amount);

        return repository.update(
                UpdatePoint.builder()
                        .pointId(point.getId())
                        .point(point.getPoint())
                        .build()
        );
    }

    /*
     * method: use
     * description: 포인트 사용
     */

    public DomainPoint use(Long userID, BigDecimal amount) {
        DomainPoint point = this.getPoint(userID);
        point.use(amount);

        return repository.update(
                UpdatePoint.builder()
                        .pointId(point.getId())
                        .point(point.getPoint())
                        .build()
        );
    }

    public DomainPoint create(Long userId) {
        return repository.create(userId);
    }
}
