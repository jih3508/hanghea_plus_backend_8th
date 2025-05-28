package kr.hhplus.be.server.infrastructure.kafka;

public final class KafkaTopics {
    public static final String ORDER_EVENTS = "order-events";
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String INVENTORY_EVENTS = "inventory-events";
    public static final String NOTIFICATION_EVENTS = "notification-events";
    
    private KafkaTopics() {
        // 유틸리티 클래스 - 인스턴스 생성 방지
    }
}
