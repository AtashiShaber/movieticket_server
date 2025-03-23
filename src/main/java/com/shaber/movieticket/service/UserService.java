package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.UserDto;
import com.shaber.movieticket.resp.AuthResponse;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.updateVO;
import com.shaber.movieticket.vo.pagequery.UserPageQueryVO;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

public interface UserService {
    //用户登录
    RV<AuthResponse> login(String uphone, String upwd);

    //用户注册
    RV registerUser(String username, String password, String phone);

    //修改密码
    RV updateUserPassword(String authHeader, updateVO updateVO) throws NoSuchAlgorithmException;

    //修改手机号
    RV updateUserPhone(String authHeader, updateVO updateVO);

    // 查询用户
    RV<PageInfo<UserDto>> listUser(UserPageQueryVO userPageQueryVO);

    RV updateUser(UserDto userDto);

    RV deleteUser(String uid);

    RV<UserDto> getUserBasic(String authHeader);

    RV recharge(String authHeader, BigDecimal money);
}
