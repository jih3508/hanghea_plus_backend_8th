package kr.hhplus.be.server.domain.user.service;

import kr.hhplus.be.server.common.dto.ApiExceptionResponse;
import kr.hhplus.be.server.domain.user.model.CreateUser;
import kr.hhplus.be.server.domain.user.model.DomainUser;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.infrastructure.user.entity.User;
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
    public DomainUser findById(Long id){
        return userRepository.findById(id).orElseThrow(() ->  new ApiExceptionResponse(HttpStatus.NOT_FOUND, "없는 사용자 입니다."));
    }

    /*
     * method: save
     * 회원 저장
     */
    public User save(String id, String name) {
        CreateUser user = new CreateUser(id, name);
        return userRepository.save(user);
    }


}
