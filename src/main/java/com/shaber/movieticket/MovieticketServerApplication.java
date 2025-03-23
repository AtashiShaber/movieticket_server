package com.shaber.movieticket;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
@SpringBootApplication
@MapperScan("com.shaber.movieticket.mapper")
@OpenAPIDefinition(info = @Info(title = "API测试", version = "1.0"))
public class MovieticketServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieticketServerApplication.class, args);
    }

}
