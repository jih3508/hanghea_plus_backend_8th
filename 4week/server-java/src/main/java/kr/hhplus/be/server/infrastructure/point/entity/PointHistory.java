package kr.hhplus.be.server.infrastructure.point.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.point.model.CreatePointHistory;
import kr.hhplus.be.server.domain.point.model.DomainPointHistory;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_history")
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
    private PointTransactionType type;

    @Column(name = "amount", precision = 10, scale = 2)
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
        this.createdDateTime = LocalDateTime.now();
    }

    public static PointHistory create(User user, PointTransactionType type, BigDecimal amount) {
        return PointHistory.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .build();
    }

    public static PointHistory create(CreatePointHistory createVo, User user){
        return PointHistory.builder()
                .user(user)
                .type(createVo.getType())
                .amount(createVo.getAmount())
                .build();
    }

    public static DomainPointHistory toDomain(PointHistory history){
        return DomainPointHistory.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .type(history.getType())
                .amount(history.getAmount())
                .build();
    }

}
