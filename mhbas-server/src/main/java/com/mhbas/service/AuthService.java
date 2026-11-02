package com.mhbas.service;

import com.mhbas.dto.UserDTO;
import com.mhbas.result.Result;
import com.mhbas.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    UserVO.Detail register(UserDTO.Register request);

    UserVO.Login login(UserDTO.Login request);

    UserVO.Detail me(Long userId);

    void logout(HttpServletRequest request);
}
