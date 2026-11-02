package com.mhbas.service.impl;

import com.mhbas.constant.RedisConstants;
import com.mhbas.properties.JwtProperties;
import com.mhbas.service.AuthService;

import com.mhbas.utils.JwtUtil;
import com.mhbas.utils.AliOssUtil;
import com.mhbas.exception.BusinessException;
import com.mhbas.dto.UserDTO;
import com.mhbas.vo.UserVO;
import com.mhbas.entity.User;
import com.mhbas.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;    // 密码加密工具
    private final AliOssUtil aliOssUtil;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthServiceImpl(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder, AliOssUtil aliOssUtil, JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.aliOssUtil = aliOssUtil;
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Transactional
    public UserVO.Detail register(UserDTO.Register request) {
        String username = request.username().trim();
        //用户名唯一
        if (userMapper.findByUsername(username) != null) {
            throw new BusinessException(409, "用户名已存在，请换一个");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .nickname(StringUtils.hasText(request.nickname()) ? request.nickname().trim() : username)
                .build();
        //防止重复插入
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(409, "用户名已存在，请换一个");
        }
        return UserVO.Detail.from(userMapper.findByUsername(username), aliOssUtil.createSignedUrl(user.getAvatarUrl()));
    }

    public UserVO.Login login(UserDTO.Login request) {
        User user = userMapper.findByUsername(request.username().trim());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId" , user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getExpiration(), claims);
        UserVO.Detail detail = UserVO.Detail.from(user, aliOssUtil.createSignedUrl(user.getAvatarUrl()));

        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        Map<String, String> loginMap = new HashMap<>();
        loginMap.put("userId", String.valueOf(user.getId()));
        loginMap.put("username", user.getUsername());
        loginMap.put("nickname", user.getNickname() == null ? "" : user.getNickname());
        stringRedisTemplate.opsForHash().putAll(tokenKey, loginMap);
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL_SECONDS, TimeUnit.SECONDS);

        return new UserVO.Login(token, detail);
    }

    public UserVO.Detail me(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return UserVO.Detail.from(user, aliOssUtil.createSignedUrl(user.getAvatarUrl()));
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = JwtUtil.resolveToken(request.getHeader(jwtProperties.getTokenName()));
        if (token != null) {
            stringRedisTemplate.delete(RedisConstants.LOGIN_USER_KEY + token);
        }
    }
}
