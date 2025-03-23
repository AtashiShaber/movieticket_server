package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.AdminDto;
import com.shaber.movieticket.resp.AuthAdminResponse;
import com.shaber.movieticket.vo.updateVO;
import com.shaber.movieticket.vo.pagequery.AdminPageQueryVO;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.AdminService;
import com.shaber.movieticket.vo.AdminAddVO;
import com.shaber.movieticket.vo.AdminLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private final AdminService adminService;

    @PostMapping("/login")
    public RV<AuthAdminResponse> login(@RequestBody AdminLoginVO adminLoginVO){
        return adminService.login(adminLoginVO.getPhone(),adminLoginVO.getAdminPwd());
    }

    @PostMapping("/register")
    public RV register(@RequestBody AdminAddVO adminAddVO){
        return adminService.register(adminAddVO.getAdminName(), adminAddVO.getAdminPwd(), adminAddVO.getPhone());
    }

    @PostMapping("/list")
    public RV<PageInfo<AdminDto>> listAdmin(@RequestBody AdminPageQueryVO pageQuery){
        return adminService.listAdmin(pageQuery);
    }

    @PostMapping("/updatePwd")
    public RV updatePw(@RequestHeader("Authorization") String authHeader,
                       @RequestBody updateVO updateVO){
        return adminService.updatePwd(authHeader, updateVO);
    }

    @PostMapping("/updatePhone")
    public RV updatePhone(@RequestHeader("Authorization") String authHeader,
                          @RequestBody updateVO updateVO){
        return adminService.updatePhone(authHeader, updateVO);
    }

    @PostMapping("/basic")
    public RV<AdminDto> basic(@RequestHeader("Authorization") String authHeader) {
        return adminService.getAdminBasic(authHeader);
    }
}
