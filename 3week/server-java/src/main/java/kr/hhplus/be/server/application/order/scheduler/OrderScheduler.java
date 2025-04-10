package kr.hhplus.be.server.application.order.scheduler;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.order.vo.OrderHistoryProductGroupVo;
import kr.hhplus.be.server.domain.product.entity.Product;
import kr.hhplus.be.server.domain.product.entity.ProductRank;
import kr.hhplus.be.server.domain.product.service.ProductRankService;
import kr.hhplus.be.server.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final OrderService orderService;

    private final ProductService productService;

    private final ProductRankService productRankService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void makeRank(){
        List<OrderHistoryProductGroupVo> list = orderService.threeDaysOrderProductHistory();
        int size = list.size();
        List<ProductRank> productRanks = new LinkedList<>();


        for (int i = 0; i < size; i++) {
            OrderHistoryProductGroupVo vo = list.get(i);
            Product product = productService.getProduct(vo.getProductId());

            ProductRank rank = ProductRank.builder()
                    .product(product)
                    .totalQuantity(vo.getTotalQuantity())
                    .rank(i + 1)
                    .build();

            productRanks.add(rank);
        }

        productRankService.save(productRanks);
    }
}
