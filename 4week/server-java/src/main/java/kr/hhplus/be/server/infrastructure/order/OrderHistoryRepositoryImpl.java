package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainOrderProductHistory;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import kr.hhplus.be.server.infrastructure.order.entity.OrderProductHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderHistoryRepositoryImpl implements OrderProductHistoryRepository {

    private final OrderProductHistoryJpaRepository repository;

    @Override
    public DomainOrderProductHistory create(CreateOrderProductHistory create) {
        return repository.save(OrderProductHistory.create(create)).toDomain();
    }

    @Override
    public List<OrderHistoryProductGroupVo> findGroupByProductIdThreeDays() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startDate = today.minusDays(3)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return repository.findGroupByProductIdThreeDays(startDate);
    }

    @Override
    public List<DomainOrderProductHistory> findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId)
                .stream().map(OrderProductHistory::toDomain).toList();
    }
}
