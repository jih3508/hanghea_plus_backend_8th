package kr.hhplus.be.server.domain.user;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /*
     * 회원 Id 조회
     */
    public User findById(Long id){
        return userRepository.findById(id).orElseThrow(() ->  new ApiExceptionResponse(HttpStatus.NOT_FOUND, "없는 사용자 입니다."));
    }
}
