package kr.hhplus.be.server.infrastructure.point.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.point.model.DomainPoint;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Point {

    private static final BigDecimal MAX_POINT = new BigDecimal(1_000_000_000L);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "point", precision = 10, scale = 2)
    private BigDecimal point;

    @Builder
    private Point(Long id, User user, BigDecimal point) {
        this.id = id;
        this.user = user;
        this.point = point;
    }

    public void setPoint(BigDecimal point) {
        this.point = point;
    }

    public static Point create(User user) {
        return Point.builder()
                .user(user)
                .point(BigDecimal.ZERO)
                .build();
    }

    public static Point create(User user, BigDecimal point) {
        return Point.builder()
                .user(user)
                .point(point)
                .build();
    }


    public DomainPoint toDomain(){
        return DomainPoint.builder()
                .id(this.id)
                .userId(this.user.getId())
                .point(this.point)
                .build();
    }

}
