package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.CreatePointHistory;
import kr.hhplus.be.server.infrastructure.point.entity.PointHistory;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository {

    void create(CreatePointHistory pointHistory);
}
