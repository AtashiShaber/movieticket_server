package com.shaber.movieticket.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.UserDto;
import com.shaber.movieticket.exception.UserServiceException;
import com.shaber.movieticket.mapper.UserMapper;
import com.shaber.movieticket.pojo.User;
import com.shaber.movieticket.resp.AuthResponse;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.UserService;
import com.shaber.movieticket.utils.JwtUtil;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.updateVO;
import com.shaber.movieticket.vo.pagequery.UserPageQueryVO;
import io.jsonwebtoken.Claims;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public RV<AuthResponse> login(String uphone, String upwd) {
        // 验证用户逻辑（原有代码不变）
        User user = userMapper.findUserByPhone(uphone);
        if (user == null || !StrUtil.equals(user.getUpwd(), SecureUtil.sha256(upwd))) {
            throw new UserServiceException("登录失败");
        }

        // 生成 UUID（前端认证标识）
        String uuid = UUID.randomUUID().toString();

        // 生成 JWT（包含 SessionID 或其他必要信息）
        String id = "ID_" + user.getUid(); // 模拟 SessionID
        String jwtToken = jwtUtil.generateToken(id);

        // 存储到 Redis（Key: UUID, Value: JWT + 用户信息）
        String redisKey = "user:token:" + uuid;
        redisTemplate.opsForValue().set(
                redisKey,
                jwtToken,
                30, TimeUnit.MINUTES  // 过期时间 30 分钟
        );

        // 返回 UUID 给前端（前端保存到 localStorage）
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);

        AuthResponse data = new AuthResponse();
        data.setUuid(uuid);
        data.setUserDto(userDto);
        data.setExpires(System.currentTimeMillis() + 30 * 60 * 1000);

        return RV.success("登录成功", data);
    }

    @Transactional
    @Override
    public RV registerUser(String username, String password, String phone) {
        if (userMapper.register(String.valueOf(snowflakeIdWorker.nextId()),username, SecureUtil.sha256(password),phone) <= 0){
            throw new UserServiceException("注册失败！");
        }
        return RV.success("注册成功！");
    }

    @Transactional
    @Override
    public RV updateUserPassword(String authHeader, updateVO updateVO) throws NoSuchAlgorithmException {
        // 通过redis获取登录的uid
        String redisKey = authHeader.replace("user:","user:token:");
        String token = redisTemplate.opsForValue().get(redisKey);
        Claims claims = jwtUtil.parseToken(token);
        String uid = claims.get("id", String.class).replaceAll("ID_", "");
        User user = userMapper.findUserByUid(uid);

        // 判断用户是否为空
        if (user == null) {
            return RV.customFail("用户信息不存在！",401);
        }

        //判断用户旧密码是否正确
        String sha256Pwd = SecureUtil.sha256(updateVO.getOldData());
        if (!StrUtil.equals(sha256Pwd,user.getUpwd())){
            throw new UserServiceException("旧密码错误");
        }

        //修改密码
        if (userMapper.updatePassword(uid,SecureUtil.sha256(updateVO.getNewData())) <= 0){
            throw new UserServiceException("密码修改出现错误！");
        }

        // 修改完成的同时清除当前token内容，用户需要重新登录
        redisTemplate.delete(redisKey);
        return RV.success("修改成功!");
    }

    @Transactional
    @Override
    public RV updateUserPhone(String authHeader, updateVO updateVO) {
        // 通过redis获取登录的uid
        String redisKey = authHeader.replace("user:","user:token:");
        String token = redisTemplate.opsForValue().get(redisKey);
        Claims claims = jwtUtil.parseToken(token);
        String uid = claims.get("id", String.class).replaceAll("ID_", "");
        User user = userMapper.findUserByUid(uid);

        // 判断用户是否为空
        if (user == null) {
            return RV.customFail("用户信息不存在！",401);
        }

        //判断该手机号是否匹配
        if (!user.getUphone().equals(updateVO.getOldData())){
            throw new UserServiceException("手机号错误！");
        }

        if (userMapper.updatePhone(uid, updateVO.getNewData()) <= 0){
            throw new UserServiceException("手机号修改错误！");
        }
        // 修改完成的同时清除当前token内容，用户需要重新登录
        redisTemplate.delete(redisKey);
        return RV.success("修改成功!");
    }

    @Override
    public RV<PageInfo<UserDto>> listUser(UserPageQueryVO userPageQueryVO) {
        PageHelper.startPage(userPageQueryVO.getPageNum(),userPageQueryVO.getPageSize());
        List<UserDto> userDtos = userMapper.listUser(userPageQueryVO.getUname(),userPageQueryVO.getUphone());
        PageInfo<UserDto> data = new PageInfo<>(userDtos);

        return RV.success("查询完成！",data);
    }

    @Transactional
    @Override
    public RV updateUser(UserDto userDto) {
        // 首先该用户是否存在
        User user = userMapper.findUserByUid(userDto.getUid());
        if (user == null){
            throw new UserServiceException("用户信息不存在！");
        }

        // 再检查是否重名
        if (!user.getUname().equals(userDto.getUname())
                && (userMapper.getUserByUname(userDto.getUname()) != null)){
            throw new UserServiceException("用户名已被注册！");
        }

        // 再检查手机号是否重复
        if (!user.getUphone().equals(userDto.getUphone())
                && (userMapper.findUserByPhone(userDto.getUphone()) != null)){
            throw new UserServiceException("该手机号已被注册！");
        }

        // 确认无误后进行修改操作
        if (userMapper.updateUser(userDto) <= 0){
            throw new UserServiceException("修改异常！");
        }

        return RV.success("修改成功！");
    }

    @Transactional
    @Override
    public RV deleteUser(String uid) {
        // 首先查询是否存在该用户
        if (userMapper.findUserByUid(uid) == null){
            throw new UserServiceException("用户不存在！");
        }

        // 用户存在则进行删除
        if (userMapper.deleteUser(uid) <= 0){
            throw new UserServiceException("删除异常！");
        }

        return RV.success("删除成功！");
    }

    @Override
    public RV<UserDto> getUserBasic(String authHeader) {
        // 通过redis获取登录的uid
        String redisKey = authHeader.replace("user:","user:token:");
        String token = redisTemplate.opsForValue().get(redisKey);
        // 如果token不存在
        if (token == null) {
            return RV.noData(401,"用户登录信息不存在！", null);
        }
        Claims claims = jwtUtil.parseToken(token);
        String uid = claims.get("id", String.class).replaceAll("ID_", "");
        User user = userMapper.findUserByUid(uid);

        // 判断用户是否为空
        if (user == null) {
            return RV.noData(401,"用户信息不存在！", null);
        }

        // 如果不为空，返回基础信息
        UserDto data = new UserDto();
        BeanUtils.copyProperties(user,data);

        return RV.success("查询成功！", data);
    }

    @Override
    public RV recharge(String authHeader, BigDecimal money) {
        // 通过redis获取登录的uid
        String redisKey = authHeader.replace("user:","user:token:");
        String token = redisTemplate.opsForValue().get(redisKey);
        // 如果token不存在
        if (token == null) {
            return RV.noData(401,"用户登录信息不存在！", null);
        }
        Claims claims = jwtUtil.parseToken(token);
        String uid = claims.get("id", String.class).replaceAll("ID_", "");
        User user = userMapper.findUserByUid(uid);

        // 判断用户是否为空
        if (user == null) {
            return RV.noData(401,"用户信息不存在！", null);
        }

        // 用户存在则进行金额的增加
        if (userMapper.addBalance(uid,money) <= 0){
            throw new UserServiceException("充值异常！");
        }

        System.out.println(uid);
        System.out.println(money);
        System.out.println(money.getClass());
        return RV.success("充值成功！");
    }
}
