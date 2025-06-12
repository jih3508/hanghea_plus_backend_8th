package kr.hhplus.be.server.common.event.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompensationRequired {
    private Long orderId;
    private Long userId;
    private List<String> completedActions; // 성공한 작업들 (보상 대상)
    private String failureReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime requestedAt;
    
    public static CompensationRequired of(Long orderId, Long userId, List<String> completedActions, String failureReason) {
        return new CompensationRequired(orderId, userId, completedActions, failureReason, LocalDateTime.now());
    }
}
