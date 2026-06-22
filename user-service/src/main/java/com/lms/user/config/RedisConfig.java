//
//package com.lms.user.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//// WHY: Centralized Redis cache configuration with per-cache TTL tuned to data change frequency
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        GenericJackson2JsonRedisSerializer serializer =
//                new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        // OPTIMIZATION: Default config with cache: prefix to distinguish from rl:* (gateway) and auth:*
//        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
//                )
//                // WHY: Prefix all app cache keys with "cache:" per hard rule in system prompt
//                .prefixCacheNameWith("cache:")
//                .entryTtl(Duration.ofMinutes(30));
//
//        // OPTIMIZATION: Per-cache TTL — resumes change more often than user profiles
//        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
//
//        // WHY: User identity data (display name, role, photo) rarely changes — 30 min safe
//        cacheConfigs.put("users:id", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        cacheConfigs.put("users:email", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//
//        // WHY: Resume data changes frequently during active editing — 10 min keeps it fresh
//        cacheConfigs.put("resumes:user", defaultConfig.entryTtl(Duration.ofMinutes(10)));
//        cacheConfigs.put("resumes:single", defaultConfig.entryTtl(Duration.ofMinutes(10)));
//
//        // WHY: Student/trainer profiles updated occasionally during onboarding — 30 min safe
//        cacheConfigs.put("student:profile", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        cacheConfigs.put("trainer:profile", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(defaultConfig)
//                .withInitialCacheConfigurations(cacheConfigs)
//                .build();
//    }
//}




package com.lms.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    // Plain ObjectMapper — used by UserService, AIResumeService, LinkedInScraperService
    // NO activateDefaultTyping — plain JSON only
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // Redis-only ObjectMapper — stores class type info for correct deserialization
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // CRITICAL: tells Jackson to store class name in Redis
        // so it knows which class to deserialize back into after TTL refresh
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        return mapper;
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                )
                .prefixCacheNameWith("cache:")
                .entryTtl(Duration.ofMinutes(30));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("users:id",       defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("users:email",    defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("resumes:user",   defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("resumes:single", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("student:profile",defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("trainer:profile",defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}