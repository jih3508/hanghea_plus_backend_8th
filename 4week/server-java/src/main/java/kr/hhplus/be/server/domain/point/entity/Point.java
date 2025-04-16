package kr.hhplus.be.server.domain.point.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.infrastructure.user.entity.User;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

@Entity
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

    public static Point create(User user) {
        return Point.builder()
                .user(user)
                .point(BigDecimal.ZERO)
                .build();
    }

    public void charge(BigDecimal amount) {
        this.point = this.point.add(amount);

        if(point.compareTo(MAX_POINT) > 0) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "충전후 포인트가 한도 초과 되었습니다.");
        }
    }


    public void use(BigDecimal amount) {
        if(point.compareTo(amount) < 0) {
            throw new ApiExceptionResponse(HttpStatus.BAD_REQUEST, "잔액 부족!!!!");
        }

        this.point = this.point.subtract(amount);
    }

}
