package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.point.entity.Point;
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
     * method: charge
     * description: 포인트 충전
     */
    public Point charge(Long userID, BigDecimal amount) {

        Point point = repository.findByUserId(userID)
                .orElseThrow(() -> new ApiExceptionResponse(HttpStatus.NOT_FOUND, "포인트를 찾을 수 없습니다."));

        point.charge(amount);

        return repository.save(point);
    }
}
