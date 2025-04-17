package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.CreatePointHistory;
import kr.hhplus.be.server.domain.point.model.DomainPointHistory;

import java.util.List;
import java.util.Optional;

public interface PointHistoryRepository {

    void create(CreatePointHistory pointHistory);

    Optional<DomainPointHistory> findByAtLast();

    List<DomainPointHistory> findByUserId(Long userId);

}
