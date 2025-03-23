package com.shaber.movieticket;

import com.shaber.movieticket.utils.JwtUtil;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class MovieticketServerApplicationTests {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateId() {
        Claims claims = jwtUtil.parseToken("eyJhbGciOiJIUzUxMiJ9.eyJpZCI6IklEXzY4NzI2MjIyNDQ0MzY0MTg1NiIsImlhdCI6MTc0MjUxNzE2MSwiZXhwIjoxNzQyNTIwNzYxfQ.CXCxFnowebK9wJsE8QCAVdo8TNf54mAV0YhIYAFoqDgpV6T66DzLjBLG9wbHX5RPSo1x53MTkF3UUn5xV0Hbtg");
        String id = claims.get("id", String.class).replace("ID_", "");
        System.out.println(id);
    }



}
