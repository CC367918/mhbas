package com.mhbas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserDTO {
    private UserDTO() {
    }

    public record Register(
            @NotBlank(message = "请输入用户名")
            @Size(min = 3, max = 50, message = "用户名长度应为 3-50 个字符")
            String username,
            @NotBlank(message = "请输入密码")
            @Size(min = 6, max = 32, message = "密码长度应为 6-32 个字符")
            String password,
            @Size(max = 50, message = "昵称不能超过 50 个字符")
            String nickname
    ) {
    }

    public record Login(
            @NotBlank(message = "请输入用户名") String username,
            @NotBlank(message = "请输入密码") String password
    ) {
    }

    public record UpdateProfile(
            @Size(max = 50, message = "昵称不能超过 50 个字符") String nickname
    ) {
    }

    public record ChangePassword(
            @NotBlank(message = "请输入原密码")
            String oldPassword,
            @NotBlank(message = "请输入新密码")
            @Size(min = 6, max = 32, message = "新密码长度应为 6-32 个字符")
            String newPassword
    ) {
    }
}
