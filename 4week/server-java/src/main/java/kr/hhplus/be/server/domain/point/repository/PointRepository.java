package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.domain.point.model.UpdatePoint;

import java.util.Optional;

public interface PointRepository {

    Optional<DomainPoint>  findByUserId(Long userId);

    DomainPoint create(Long userId);

    DomainPoint update(UpdatePoint updatePoint);

    void delete(Long userId);



}
