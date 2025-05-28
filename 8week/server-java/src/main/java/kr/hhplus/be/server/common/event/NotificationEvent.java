package kr.hhplus.be.server.common.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NotificationEvent extends BaseEvent {
    private Long userId;
    private String notificationType;
    private String title;
    private String message;
    private String channel; // EMAIL, SMS, PUSH

    @JsonCreator
    public NotificationEvent(
            @JsonProperty("userId") Long userId,
            @JsonProperty("notificationType") String notificationType,
            @JsonProperty("title") String title,
            @JsonProperty("message") String message,
            @JsonProperty("channel") String channel) {
        super("NOTIFICATION", "ecommerce-notification-service");
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.channel = channel;
    }
}
