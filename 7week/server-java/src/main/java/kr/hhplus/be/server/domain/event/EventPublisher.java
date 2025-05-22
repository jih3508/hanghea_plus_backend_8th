package kr.hhplus.be.server.domain.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final ApplicationEventPublisher publisher;

    public void publish(Object event) {
        publisher.publishEvent(event);
    }
}
