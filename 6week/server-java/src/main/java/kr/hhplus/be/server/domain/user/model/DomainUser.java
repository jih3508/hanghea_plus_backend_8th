package kr.hhplus.be.server.domain.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DomainUser {

    private Long id; // pk

    private String userId; // 사용자 id

    private String name; // 사용자명

    private LocalDateTime createDateTime; // 생성 일시

    private LocalDateTime updateDateTime; // 수정 일시


}
