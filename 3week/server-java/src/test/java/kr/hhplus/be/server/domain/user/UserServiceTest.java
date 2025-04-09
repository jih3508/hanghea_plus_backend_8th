package kr.hhplus.be.server.domain.user;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    @DisplayName("없는 사용자 이면 없다고 오류를 보내야 한다.")
    void 사용자_없는_경우(){

        //given, when
        when(userRepository.findById(anyLong())).thenThrow( new ApiExceptionResponse(HttpStatus.NOT_FOUND, "없는 사용자 입니다."));


        // then
        assertThatThrownBy(() -> userService.findById(10L))
                .isInstanceOf(ApiExceptionResponse.class)
                .hasMessage("없는 사용자 입니다.");

    }

    @Test
    @DisplayName("사용자가 있으면 User 객체를 전달 해줘야 한다.")
    void 사용자_있는_경우(){
        // given
        User user = User.builder()
                .id(1L)
                .userId("test")
                .name("테스터")
                .build();

        Long id = 1L;

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = userService.findById(1L);

        // then
        verify(userRepository, times(1)).findById(1L);
        assertThat(result).isEqualTo(user);

    }
}