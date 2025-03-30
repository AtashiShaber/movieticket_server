package com.shaber.movieticket.config;

import com.shaber.movieticket.filter.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/admin/login",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/swagger-ui/**"
                );
    }

    // 跨域配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许前端域名和端口（开发环境）
                .allowedOriginPatterns("http://49.235.28.76", "http://localhost:*", "http://127.0.0.1:*")
                // 允许所有 HTTP 方法
                .allowedMethods("*")
                // 允许携带的请求头（必须包含 Authorization）
                .allowedHeaders("Authorization", "Content-Type")
                // 允许前端读取的响应头（如自定义 Token 刷新头）
                .exposedHeaders("new-token")
                // 允许携带凭证（Cookies、Authorization 头等）
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }
}
