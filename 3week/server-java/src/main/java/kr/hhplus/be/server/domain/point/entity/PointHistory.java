package kr.hhplus.be.server.domain.point.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    PointTransactionType type;

    private BigDecimal amount;

    @CreatedDate
    @Column(name = "create_date_time", updatable = false)
    private LocalDateTime createdDateTime; // 생성 일시

    @Builder
    public PointHistory(Long id, User user, PointTransactionType type, BigDecimal amount) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.amount = amount;
    }

    public static PointHistory create(User user, PointTransactionType type, BigDecimal amount) {
        return PointHistory.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .build();
    }

}
