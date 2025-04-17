package kr.hhplus.be.server.infrastructure.user.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // pk

    @Column(name = "user_id", unique = true)
    private String userId; // 사용자 id

    private String name; // 사용자명

    @CreatedDate
    @Column(name = "create_date_time", updatable = false)
    private LocalDateTime createDateTime; // 생성 일시

    @LastModifiedDate
    @Column(name = "update_date_time", updatable = true)
    private LocalDateTime updateDateTime; // 수정 일시

    @Builder
    private  User(Long id, String userId, String name){
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.createDateTime = LocalDateTime.now();
    }

    public static User create(CreateUser createUser) {
        return User.builder()
                .userId(createUser.getId())
                .name(createUser.getName())
                .build();
    }

    public DomainUser toDomain(){
        return DomainUser.builder()
                .id(this.id)
                .name(this.name)
                .createDateTime(this.createDateTime)
                .updateDateTime(this.updateDateTime)
                .build();
    }

}
