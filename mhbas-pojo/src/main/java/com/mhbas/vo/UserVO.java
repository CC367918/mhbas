package com.mhbas.vo;

import com.mhbas.entity.User;

public final class UserVO {
    private UserVO() {
    }

    // 用户详情
    public record Detail(Long id, String username, String nickname , String avatarUrl, boolean wxBound) {
        public static Detail from(User user, String avatarUrl) {
            return new Detail(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    avatarUrl,
                    user.getWxOpenid() != null && !user.getWxOpenid().isBlank()
            );
        }
    }

    // 登录结果
    public record Login(String token, Detail detail) {
    }

    // 头像信息
    public record Avatar(String url) {
    }
}
