package com.mhbas.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;    // 昵称
    private String avatarUrl;    // 头像地址
    private String wxOpenid;    // 微信openid
}
