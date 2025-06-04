package kr.hhplus.be.server.domain.order.model;

public enum OrderStatus {
    PENDING,     // 주문 접수됨 (처리 대기중)
    COMPLETED,   // 주문 완료
    CANCELLED    // 주문 취소됨
}
