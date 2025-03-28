package com.shaber.movieticket.filter;

import cn.hutool.core.util.StrUtil;
import com.shaber.movieticket.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    // 不需要认证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            // Swagger 相关路径
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/user/login",
            "/user/register",
            "/admin/login",
            "/movie/list", "/movie/listAdmin", "/movie/listUpCast",
            "/movie/auto", "/movie/upComing", "/movie/getCast",
            "/cinema/list", "/cinema/listAll",
            "/screening/list", "/screening/listByCid",
            "/screenroom/list", "/screenroom/listAll", "/screenroom/listCid"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 关键：放行 OPTIONS 预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String path = request.getServletPath();

        // 排除不需要认证的路径
        if (EXCLUDE_PATHS.contains(path)) {
            return true;
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            sendUnauthorizedResponse(response, "请先登录");
            return false;
        }
        String uuid = token.split(":")[1];
        String role = token.split(":")[0];

        // 验证Redis中的Token
        String redisKey = "";
        if ("user".equals(role)) {
            redisKey = "user:token:" + uuid;

        }
        if ("admin".equals(role)) {
            redisKey = "admin:token:" + uuid;
        }
        System.out.println("uuid: " + uuid + " role:" + role + " redisKey:" + redisKey);
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isBlank(redisValue)) {
            sendUnauthorizedResponse(response, "登录已过期");
            return false;
        }

        // 解析JWT
        try {
            String jwtToken = redisValue.split("\\|")[0];
            Claims claims = jwtUtil.parseToken(jwtToken);

            // Token刷新逻辑（剩余5分钟时刷新）
            long expire = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            if (expire > 0 && expire < 300) {
                String newUuid = UUID.randomUUID().toString();
                String newJwt = jwtUtil.generateToken(claims.getSubject());

                // 更新Redis
                if ("user".equals(role)) {
                    redisTemplate.opsForValue().set(
                            "user:token:" + newUuid,
                            newJwt + "|" + claims.getSubject(),
                            30, TimeUnit.MINUTES
                    );
                }
                if ("admin".equals(role)) {
                    redisTemplate.opsForValue().set(
                            "admin:token:" + newUuid,
                            newJwt + "|" + claims.getSubject(),
                            30, TimeUnit.MINUTES
                    );
                }
                redisTemplate.delete(redisKey);

                // 返回新Token
                response.setHeader("new-token", newUuid);
            }
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "无效的Token");
            return false;
        }

        return true;
    }

    // 统一返回401响应
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401, \"message\":\"" + message + "\"}");
    }
}
