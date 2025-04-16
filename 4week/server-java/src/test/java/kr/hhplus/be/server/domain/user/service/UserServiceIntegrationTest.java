package kr.hhplus.be.server.domain.user.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.support.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class UserServiceIntegrationTest  extends IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceIntegrationTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 조회 시, 사용자 정보 없으면 오류 보낸다.")
    void 없는_사용자_조회(){
        //given
        Long id= 10L;


        // when && that
        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("없는 사용자 입니다.");

    }

    @Test
    @DisplayName("사욪자 조회")
    void 사용자_조회(){
        //given
        Long id= 1L;
        CreateUser user = CreateUser.builder()
                .name("홍길동")
                .id("test")
                .build();

        userRepository.save(user);

        // when
        DomainUser result  = userService.findById(id);

        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getUserId()).isEqualTo("test");

    }


}
