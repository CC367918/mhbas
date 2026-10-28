package com.mhbas.service;

import com.mhbas.dto.UserDTO;
import com.mhbas.vo.UserVO;

public interface AuthService {
    UserVO.Detail register(UserDTO.Register request);

    UserVO.Login login(UserDTO.Login request);

    UserVO.Detail me(Long userId);
}
