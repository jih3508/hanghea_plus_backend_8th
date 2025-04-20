package kr.hhplus.be.server.domain.order.service;

import kr.hhplus.be.server.domain.order.model.CreateOrder;
import kr.hhplus.be.server.domain.order.model.CreateOrderProductHistory;
import kr.hhplus.be.server.domain.order.model.DomainOrder;
import kr.hhplus.be.server.infrastructure.order.entity.Order;
import kr.hhplus.be.server.domain.order.repository.OrderProductHistoryRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    private final OrderProductHistoryRepository historyRepository;


    public DomainOrder create(CreateOrder  createOrder) {

        DomainOrder domainOrder = repository.create(createOrder);
        domainOrder.getItems().forEach(item -> {
           CreateOrderProductHistory  history = new CreateOrderProductHistory(domainOrder.getId(), item.getProductId(), item.getQuantity());
           historyRepository.create(history);
        });
        return repository.create(createOrder);
    }


    /*
     * method: threeDaysOrderProductHistory
     * description: 3일안 주문한 상품 정보 가져오기
     */
    public List<OrderHistoryProductGroupVo> threeDaysOrderProductHistory(){
        return historyRepository.findGroupByProductIdThreeDays();
    }

}
