package com.mhbas.controller;

import com.mhbas.annotation.OperationLog;
import com.mhbas.result.Result;
import com.mhbas.dto.UserDTO;
import com.mhbas.vo.UserVO;
import com.mhbas.dto.UserDTO;
import com.mhbas.vo.UserVO;
import com.mhbas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证相关
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @OperationLog("用户注册")
    public Result<UserVO.Detail> register(@Valid @RequestBody UserDTO.Register request) {
        UserVO.Detail detail = authService.register(request);
        return Result.success(detail);
    }

    @PostMapping("/login")
    @OperationLog("用户登录")
    public Result<UserVO.Login> login(@Valid @RequestBody UserDTO.Login request) {
        UserVO.Login login = authService.login(request);
        return Result.success(login);
    }

    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }
}
