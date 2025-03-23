package com.shaber.movieticket.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.AdminDto;
import com.shaber.movieticket.exception.AdminServiceException;
import com.shaber.movieticket.mapper.AdminMapper;
import com.shaber.movieticket.pojo.Admin;
import com.shaber.movieticket.resp.AuthAdminResponse;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.AdminService;
import com.shaber.movieticket.utils.JwtUtil;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.updateVO;
import com.shaber.movieticket.vo.pagequery.AdminPageQueryVO;
import io.jsonwebtoken.Claims;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminMapper adminMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public RV<AuthAdminResponse> login(String phone, String adminPwd) {
        // 先查询是否存在该手机号
        Admin admin = adminMapper.findAdminByPhone(phone);
        if (admin == null){
            throw new AdminServiceException("该手机号的管理员不存在！");
        }
        // 转换密码为sha256形式
        String sha256Pwd = SecureUtil.sha256(adminPwd);
        // 进行密码配对
        if (!StrUtil.equals(admin.getAdminPwd(), sha256Pwd)){
            throw new AdminServiceException("密码输入错误！");
        }
        // 生成 UUID（前端认证标识）
        String uuid = UUID.randomUUID().toString();

        // 生成 JWT（包含 SessionID 或其他必要信息）
        String id = "ID_" + admin.getAdminId(); // 模拟 SessionID
        String jwtToken = jwtUtil.generateToken(id);

        // 存储到 Redis（Key: UUID, Value: JWT + 用户信息）
        String redisKey = "admin:token:" + uuid;
        redisTemplate.opsForValue().set(
                redisKey,
                jwtToken,
                30, TimeUnit.MINUTES  // 过期时间 30 分钟
        );

        // 返回 UUID 给前端（前端保存到 localStorage）
        AdminDto adminDto = new AdminDto();
        BeanUtils.copyProperties(admin, adminDto);

        AuthAdminResponse data = new AuthAdminResponse();
        data.setUuid(uuid);
        data.setAdminDto(adminDto);
        data.setExpires(System.currentTimeMillis() + 30 * 60 * 1000);

        return RV.success("登录成功", data);
    }

    @Transactional
    @Override
    public RV register(String adminName, String adminPwd, String phone) {
        // 先查询是否存在手机号或用户名重复问题
        if (adminMapper.findAdminByPhone(phone) != null){
            throw new AdminServiceException("该手机号已被注册！");
        }
        if (adminMapper.findAdminByName(adminName) != null){
            throw new AdminServiceException("用户名已存在！");
        }

        // 若不存在重复问题则进行注册
        if (adminMapper.register(String.valueOf(snowflakeIdWorker.nextId()),adminName,SecureUtil.sha256(adminPwd),phone) <= 0){
            throw new AdminServiceException("注册失败！");
        }

        return RV.success("注册成功！");
    }

    @Override
    public RV<PageInfo<AdminDto>> listAdmin(AdminPageQueryVO pageQuery) {
        PageHelper.startPage(pageQuery.getPageNum(),pageQuery.getPageSize());
        List<AdminDto> listAdmin = adminMapper.listAdmin(pageQuery.getAdminName(),pageQuery.getPhone());
        PageInfo<AdminDto> data = new PageInfo<>(listAdmin);
        return RV.success("查询完毕",data);
    }

    @Transactional
    @Override
    public RV updatePwd(String authHeader, updateVO updateVO) {
        // 获取redis中的主要内容
        String redisKey = "admin:token:" + authHeader.replaceAll("admin:","");
        String s = redisTemplate.opsForValue().get(redisKey);
        // 如果s为null
        if (StrUtil.isBlank(s)) {
            return RV.customFail("用户登录信息不存在！",401);
        }
        Claims claims = jwtUtil.parseToken(s);
        String adminId = claims.get("id", String.class).replace("ID_", "");

        // 通过adminId查询用户信息
        Admin admin = adminMapper.findAdmin(adminId);
        // 判断用户是否存在
        if (admin == null) {
            return RV.customFail("用户信息不存在！",401);
        }
        // 比对旧密码是否一致
        if (!admin.getAdminPwd().equals(SecureUtil.sha256(updateVO.getOldData()))) {
            throw new AdminServiceException("密码错误！");
        }

        // 如果一致则进行密码修改操作
        if (adminMapper.updatePwd(adminId, SecureUtil.sha256(updateVO.getNewData())) <= 0) {
            throw new AdminServiceException("密码修改异常！");
        }

        // 修改完成的同时清除当前token内容，用户需要重新登录
        redisTemplate.delete(redisKey);
        return RV.success("修改成功！");
    }

    @Override
    public RV<AdminDto> getAdminBasic(String authHeader) {
        // 获取redis中的主要内容
        String redisKey = "admin:token:" + authHeader.replaceAll("admin:","");
        String s = redisTemplate.opsForValue().get(redisKey);
        // 如果s为null
        if (StrUtil.isBlank(s)) {
            return RV.noData(401,"用户登录信息不存在！", null);
        }
        Claims claims = jwtUtil.parseToken(s);
        String adminId = claims.get("id", String.class).replace("ID_", "");

        // 通过adminId查询用户信息
        Admin admin = adminMapper.findAdmin(adminId);
        // 判断用户是否存在
        if (admin == null) {
            return RV.noData(401,"用户信息不存在！", null);
        }

        // 返回基本信息
        AdminDto data = new AdminDto();
        BeanUtils.copyProperties(admin, data);

        return RV.success("查询完毕！",data);
    }

    @Transactional
    @Override
    public RV updatePhone(String authHeader, updateVO updateVO) {
        // 获取redis中的主要内容
        String redisKey = "admin:token:" + authHeader.replaceAll("admin:","");
        String s = redisTemplate.opsForValue().get(redisKey);
        // 如果s为null
        if (StrUtil.isBlank(s)) {
            return RV.noData(401,"用户登录信息不存在！", null);
        }
        Claims claims = jwtUtil.parseToken(s);
        String adminId = claims.get("id", String.class).replace("ID_", "");

        // 通过adminId查询用户信息
        Admin admin = adminMapper.findAdmin(adminId);
        // 判断用户是否存在
        if (admin == null) {
            return RV.noData(401,"用户信息不存在！", null);
        }

        // 存在则进行判断手机号是否一致
        if (!admin.getPhone().equals(updateVO.getOldData())) {
            return RV.fail("手机号输入错误！");
        }

        // 判断一致则进行修改操作
        if (adminMapper.updatePhone(adminId, updateVO.getNewData()) <= 0) {
            throw new AdminServiceException("手机号修改异常！");
        }

        // 修改完成的同时清除当前token内容，用户需要重新登录
        redisTemplate.delete(redisKey);
        return RV.success("修改完成！");
    }

}
