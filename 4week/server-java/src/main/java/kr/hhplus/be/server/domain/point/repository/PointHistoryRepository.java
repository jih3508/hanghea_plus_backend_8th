package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.CreatePointHistory;

public interface PointHistoryRepository {

    void create(CreatePointHistory pointHistory);
}
