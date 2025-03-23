package com.shaber.movieticket.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig {
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        StringRedisSerializer srs = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jjrs = new Jackson2JsonRedisSerializer<>(Object.class);

        redisTemplate.setKeySerializer(srs);
        redisTemplate.setValueSerializer(jjrs);

        redisTemplate.setHashKeySerializer(srs);
        redisTemplate.setHashValueSerializer(jjrs);

        return redisTemplate;
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 支持 Java 8 时间类型
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 转为原类型
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY // 生成 @class 字段
        );

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper) // 使用自定义 ObjectMapper
                ));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

}
