package com.mhbas.service.impl;

import com.mhbas.service.UserService;

import com.mhbas.exception.BusinessException;
import com.mhbas.dto.UserDTO;
import com.mhbas.vo.UserVO;
import com.mhbas.entity.User;
import com.mhbas.mapper.UserMapper;
import com.mhbas.utils.AliOssUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AliOssUtil aliOssUtil;

    public UserServiceImpl(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder, AliOssUtil aliOssUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.aliOssUtil = aliOssUtil;
    }

    @Transactional
    public UserVO.Detail updateProfile(Long userId, UserDTO.UpdateProfile newProfile) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (newProfile.nickname() != null){
            user.setNickname(newProfile.nickname());
        }
        userMapper.updateProfile(user);
        return UserVO.Detail.from(user, aliOssUtil.createSignedUrl(user.getAvatarUrl()));
    }

    @Transactional
    public void changePassword(Long userId, UserDTO.ChangePassword request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // matches：把"明文密码"和"数据库里存的哈希"对比，验证是否匹配
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        // encode：把明文密码哈希成密文，存入数据库
        userMapper.updatePassword(userId, passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public UserVO.Detail updateAvatar(Long userId, String avatarUrl) {
        userMapper.updateAvatar(userId, avatarUrl);
        return UserVO.Detail.from( userMapper.findById(userId), aliOssUtil.createSignedUrl(avatarUrl) );
    }
}
