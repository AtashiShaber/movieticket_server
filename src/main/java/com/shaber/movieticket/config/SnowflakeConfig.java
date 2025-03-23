package com.shaber.movieticket.config;

import com.shaber.movieticket.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    @Value("${snowflake.datacenter-id:1}")
    private long datacenterId;

    @Value("${snowflake.machine-id:1}")
    private long machineId;

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(datacenterId, machineId);
    }
}

