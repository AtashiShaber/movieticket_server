package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.UserDto;
import com.shaber.movieticket.resp.AuthResponse;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.impl.UserServiceImpl;
import com.shaber.movieticket.vo.MoneyVO;
import com.shaber.movieticket.vo.updateVO;
import com.shaber.movieticket.vo.UserAddVO;
import com.shaber.movieticket.vo.UserLoginVO;
import com.shaber.movieticket.vo.pagequery.UserPageQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserServiceImpl userService;

    @PostMapping("/login")
    public RV<AuthResponse> login(@RequestBody UserLoginVO userLoginVO){
        return userService.login(userLoginVO.getUphone(), userLoginVO.getUpwd());
    }

    @PostMapping("/register")
    public RV register(@RequestBody UserAddVO userAddVO){
        return userService.registerUser(userAddVO.getUname(), userAddVO.getUpwd(), userAddVO.getUphone());
    }

    @PostMapping("/list")
    public RV<PageInfo<UserDto>> list(@RequestBody UserPageQueryVO userPageQueryVO){
        return userService.listUser(userPageQueryVO);
    }

    @PostMapping("/update")
    public RV update(@RequestBody UserDto userDto){
        return userService.updateUser(userDto);
    }


    @PostMapping("/updatePwd")
    public RV updatePwd(@RequestHeader("Authorization") String authHeader,
                        @RequestBody updateVO updateVO) throws NoSuchAlgorithmException {
        return userService.updateUserPassword(authHeader, updateVO);
    }

    @PostMapping("/updatePhone")
    public RV updatePhone(@RequestHeader("Authorization") String authHeader,
                          @RequestBody updateVO updateVO) {
        return userService.updateUserPhone(authHeader, updateVO);
    }

    @PostMapping("/delete")
    public RV delete(@RequestBody Map<String,String> userMap){
        return userService.deleteUser(userMap.get("uid"));
    }

    @PostMapping("/basic")
    public RV<UserDto> basic(@RequestHeader("Authorization") String authHeader){
        return userService.getUserBasic(authHeader);
    }

    @PostMapping("/recharge")
    public RV recharge(@RequestHeader("Authorization") String authHeader, @RequestBody MoneyVO moneyVO){
        return userService.recharge(authHeader, moneyVO.getMoney());
    }
}
