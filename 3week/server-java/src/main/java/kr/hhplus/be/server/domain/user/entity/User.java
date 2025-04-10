package kr.hhplus.be.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@Entity
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
    private LocalDateTime updateDateTime; // 수정 일시

    @Builder
    private  User(Long id, String userId, String name){
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.createDateTime = LocalDateTime.now();
    }
}
