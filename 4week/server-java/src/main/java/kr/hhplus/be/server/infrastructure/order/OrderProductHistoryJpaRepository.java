package kr.hhplus.be.server.infrastructure.order;

import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import kr.hhplus.be.server.infrastructure.order.entity.OrderProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderProductHistoryJpaRepository extends JpaRepository<OrderProductHistory, Long> {

    @Query("SELECT o.productId AS productId, SUM(o.quantity) AS totalQuantity " +
            "FROM OrderProductHistory o " +
            "WHERE o.createDateTime >= :startDate " +
            "GROUP BY o.productId " +
            "ORDER BY SUM(o.quantity) DESC")
    List<OrderHistoryProductGroupVo> findGroupByProductIdThreeDays(LocalDateTime startDate);

    List<OrderProductHistory> findByOrderId(Long orderId);
}
