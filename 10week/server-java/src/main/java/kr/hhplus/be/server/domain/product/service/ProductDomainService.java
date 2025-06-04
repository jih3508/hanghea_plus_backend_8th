package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.model.DomainProduct;
import kr.hhplus.be.server.domain.product.model.DomainProductStock;
import kr.hhplus.be.server.domain.product.repository.ProductRepository;
import kr.hhplus.be.server.domain.product.repository.ProductStockRepository;
import kr.hhplus.be.server.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDomainService {
    
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final OrderRepository orderRepository;
    
    /**
     * 재고 차감 처리
     */
    @Transactional
    public boolean deductStock(Long productId, int quantity) {
        try {
            DomainProductStock stock = productStockRepository.findByProductId(productId);
            if (stock == null) {
                log.warn("상품 재고 정보를 찾을 수 없음 - productId: {}", productId);
                return false;
            }
            
            if (stock.getStock() < quantity) {
                log.warn("재고 부족 - productId: {}, 현재재고: {}, 요청수량: {}", 
                        productId, stock.getStock(), quantity);
                return false;
            }
            
            // 재고 차감
            stock.deduct(quantity);
            productStockRepository.save(stock);
            
            log.info("재고 차감 완료 - productId: {}, 차감수량: {}, 남은재고: {}", 
                    productId, quantity, stock.getStock());
            return true;
            
        } catch (Exception e) {
            log.error("재고 차감 중 오류 발생 - productId: {}, quantity: {}", productId, quantity, e);
            return false;
        }
    }
    
    /**
     * 주문 취소시 재고 복원
     */
    @Transactional
    public boolean restoreStockForOrder(Long orderId) {
        try {
            // 주문에서 차감된 재고 정보 조회 및 복원
            // 실제로는 주문 아이템들을 조회해서 각각 복원해야 함
            var orderItems = orderRepository.findOrderItemsByOrderId(orderId);
            
            for (var orderItem : orderItems) {
                DomainProductStock stock = productStockRepository.findByProductId(orderItem.getProductId());
                if (stock != null) {
                    stock.restore(orderItem.getQuantity());
                    productStockRepository.save(stock);
                    
                    log.info("재고 복원 완료 - productId: {}, 복원수량: {}, 현재재고: {}", 
                            orderItem.getProductId(), orderItem.getQuantity(), stock.getStock());
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("재고 복원 중 오류 발생 - orderId: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * 상품 정보 조회
     */
    public DomainProduct getProduct(Long productId) {
        DomainProduct product = productRepository.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
        }
        return product;
    }
}
