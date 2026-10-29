package com.mhbas.controller;

import com.mhbas.annotation.OperationLog;
import com.mhbas.context.BaseContext;
import com.mhbas.result.Result;
import com.mhbas.dto.UserDTO;
import com.mhbas.vo.UserVO;
import com.mhbas.service.AuthService;
import com.mhbas.service.FileStorageService;
import com.mhbas.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthService authService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(AuthService authService, UserService userService, FileStorageService fileStorageService) {
        this.authService = authService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("/me")
    public Result<UserVO.Detail> me() {
        UserVO.Detail detail=authService.me(BaseContext.requireCurrentId());
        return Result.success(detail);
    }

    @PutMapping("/me")
    @OperationLog("更新个人资料")
    public Result<UserVO.Detail> updateProfile(@Valid @RequestBody UserDTO.UpdateProfile newProfile) {
        UserVO.Detail detail=userService.updateProfile(BaseContext.requireCurrentId(), newProfile);
        return Result.success(detail);
    }
    
    @PutMapping("/me/password")
    @OperationLog("修改登录密码")
    public Result changePassword(@Valid @RequestBody UserDTO.ChangePassword request) {
        userService.changePassword(BaseContext.requireCurrentId(), request);
        return Result.success();
    }

    @PostMapping("/me/avatar")
    @OperationLog("上传头像")
    public Result<UserVO.Avatar> uploadAvatar(@RequestParam MultipartFile file) {
        Long userId = BaseContext.requireCurrentId();
        String avatarUrl = fileStorageService.uploadAvatar(userId, file);
        UserVO.Detail user = userService.updateAvatar(userId, avatarUrl);
        return Result.success(new UserVO.Avatar(user.avatarUrl()));
    }
}
