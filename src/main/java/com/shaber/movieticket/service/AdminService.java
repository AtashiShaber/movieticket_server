package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.AdminDto;
import com.shaber.movieticket.resp.AuthAdminResponse;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.updateVO;
import com.shaber.movieticket.vo.pagequery.AdminPageQueryVO;

public interface AdminService {
    // 负责管理员的手机号登录操作
    RV<AuthAdminResponse> login(String phone, String adminPwd);

    RV register(String adminName, String adminPwd, String phone);

    RV<PageInfo<AdminDto>> listAdmin(AdminPageQueryVO pageQuery);

    RV updatePwd(String authHeader, updateVO updateVO);

    RV<AdminDto> getAdminBasic(String authHeader);

    RV updatePhone(String authHeader, updateVO updateVO);
}
