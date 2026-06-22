//// OPTIMIZATION: New config class to set per-cache TTL values using RedisCacheManager.
//// Without this, @Cacheable uses Spring's default TTL (no expiry) which means
//// cached org data never evicts automatically — only manual @CacheEvict would clear it.
//// 
//// Cache names and TTLs:
//// - "org"  → 5 minutes (single org detail, admin dashboard)
//// - "orgs" → 10 minutes (public org list for student signup dropdown)
////
//// Key prefix uses "cache:" as required by project Redis rules.
//
//package com.lms.auth.config;
//
//import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//
//@Configuration
//public class CacheConfig {
//
//    @Bean
//    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
//        return builder -> {
//            // Base config: key prefix "cache:", JSON value serializer
//            RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
//                .prefixCacheNameWith("cache:")
//                .serializeKeysWith(RedisSerializationContext.SerializationPair
//                    .fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair
//                    .fromSerializer(new GenericJackson2JsonRedisSerializer()));
//
//            // "org" cache — single org by ID, 5-minute TTL
//            builder.withCacheConfiguration("org",
//                base.entryTtl(Duration.ofMinutes(5)));
//
//            // "orgs" cache — public org list for dropdown, 10-minute TTL
//            builder.withCacheConfiguration("orgs",
//                base.entryTtl(Duration.ofMinutes(10)));
//        };
//    }
//}


package com.lms.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.Duration;

@Configuration
public class CacheConfig {

    // OPTIMIZATION: Uses the same ObjectMapper bean from RedisConfig
    // which has JavaTimeModule registered.
    // Both RedisTemplate and CacheManager now use identical serialization —
    // no mismatch between how data is written vs how it is read back.
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
    		
            
    		@Qualifier("redisObjectMapper")		ObjectMapper redisObjectMapper) {

        GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration base = RedisCacheConfiguration
            .defaultCacheConfig()
            .prefixCacheNameWith("cache:")
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair
                    .fromSerializer(serializer));

        return builder -> {
            // "org" — single org by ID, 5 minute TTL
            builder.withCacheConfiguration("org",
                base.entryTtl(Duration.ofMinutes(5)));

            // "orgs" — public org list for signup dropdown, 10 minute TTL
            builder.withCacheConfiguration("orgs",
                base.entryTtl(Duration.ofMinutes(10)));
        };
    }
}