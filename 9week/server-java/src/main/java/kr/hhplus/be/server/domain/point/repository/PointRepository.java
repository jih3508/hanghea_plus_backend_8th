package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.UpdatePoint;

import java.math.BigDecimal;
import java.util.Optional;

public interface PointRepository {

    Optional<DomainPoint> findByUserIdLock(Long userId);

    DomainPoint create(Long userId);

    DomainPoint create(Long userId, BigDecimal point);

    DomainPoint update(UpdatePoint updatePoint);

    DomainPoint save(DomainPoint domainPoint);

    void delete(Long userId);

    Optional<DomainPoint> findByUserId(Long userId);

}
