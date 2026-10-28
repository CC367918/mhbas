package com.mhbas.mapper;

import com.mhbas.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(String username);

    User findById(Long id);

    void insert(User user);

    int updateProfile(User user);

    int updateAvatar(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int updateWxOpenid(@Param("id") Long id, @Param("wxOpenid") String wxOpenid);
}
