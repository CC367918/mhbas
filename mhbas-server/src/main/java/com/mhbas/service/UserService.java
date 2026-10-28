package com.mhbas.service;

import com.mhbas.dto.UserDTO;
import com.mhbas.vo.UserVO;

public interface UserService {
    UserVO.Detail updateProfile(Long userId, UserDTO.UpdateProfile newProfile);

    void changePassword(Long userId, UserDTO.ChangePassword request);

    UserVO.Detail updateAvatar(Long userId, String avatarUrl);
}
