package com.shaber.movieticket.resp;

import com.shaber.movieticket.dto.UserDto;
import lombok.Data;

@Data
public class AuthResponse {
    private String uuid;      // 前端认证标识
    private UserDto userDto; // 用户数据
    private long expires;     // 过期时间戳

}
