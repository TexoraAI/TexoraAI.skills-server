//
//package com.lms.course.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//// OPTIMIZATION: Rewrote RedisConfig to use dual-ObjectMapper pattern required for
//// correct Redis deserialization after TTL expiry.
////
//// ROOT CAUSE OF PREVIOUS BUG:
////   Old config stored plain JSON e.g. {"id":1,"title":"Java"} — after TTL expiry
////   Jackson could not determine which class to deserialize into → silent 500 errors
////   or redirect to login page.
////
//// FIX:
////   Bean 1 (@Primary)  — plain ObjectMapper for Services/Controllers (no type info)
////   Bean 2 (redisObjectMapper) — typed ObjectMapper for Redis only
////     stores: ["com.lms.course.model.Course",{"id":1,"title":"Java"}]
////     Jackson reads class name → correct deserialization every time.
////
//// ALSO FIXED:
////   - Added @EnableCaching (was missing — caching was silently disabled)
////   - Added prefixCacheNameWith("cache:") to comply with key prefix rules
////   - Added per-cache TTL overrides for content and course caches
//
//@Configuration
//@EnableCaching
//public class RedisConfig {
//
//    // ── Bean 1: Primary plain ObjectMapper ────────────────────────────────────
//    // Used by all Services, Controllers, JSON parsing.
//    // NO activateDefaultTyping — plain JSON only.
//    @Bean
//    @Primary
//    public ObjectMapper objectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        return mapper;
//    }
//
//    // ── Bean 2: Redis-only ObjectMapper ───────────────────────────────────────
//    // activateDefaultTyping REQUIRED — stores class type info so Redis can
//    // deserialize correctly after TTL expiry.
//    @Bean("redisObjectMapper")
//    public ObjectMapper redisObjectMapper() {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//        mapper.activateDefaultTyping(
//            mapper.getPolymorphicTypeValidator(),
//            ObjectMapper.DefaultTyping.NON_FINAL
//        );
//        return mapper;
//    }
//
//    @Bean
//    public RedisCacheManager cacheManager(
//            RedisConnectionFactory connectionFactory,
//            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
//
//        GenericJackson2JsonRedisSerializer serializer =
//                new GenericJackson2JsonRedisSerializer(redisObjectMapper);
//
//        // Default config — applies to any cache not listed in overrides below
//        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                    RedisSerializationContext.SerializationPair.fromSerializer(serializer))
//                // OPTIMIZATION: prefix "cache:" complies with key namespace rules:
//                //   rl:*    → API Gateway rate limiting (do not touch)
//                //   cache:* → application caching (this service)
//                //   auth:*  → reset/verification tokens only
//                .prefixCacheNameWith("cache:")
//                .entryTtl(Duration.ofMinutes(30));
//
//        // Per-cache TTL overrides
//        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
//
//        // content list per course — evicted on any content write; 15 min is safe
//        cacheConfigs.put("contentByCourse",
//                defaultConfig.entryTtl(Duration.ofMinutes(15)));
//
//        // course by id — evicted on update/delete; 20 min is safe
//        cacheConfigs.put("courseById",
//                defaultConfig.entryTtl(Duration.ofMinutes(20)));
//
//        // trainer's own course list — evicted on create; 15 min is safe
//        cacheConfigs.put("coursesByEmail",
//                defaultConfig.entryTtl(Duration.ofMinutes(15)));
//
//        // org course list — evicted on create; 15 min is safe
//        cacheConfigs.put("coursesByOrg",
//                defaultConfig.entryTtl(Duration.ofMinutes(15)));
//
//        // category list — rarely changes; 30 min default is fine
//        cacheConfigs.put("allCategories",
//                defaultConfig.entryTtl(Duration.ofMinutes(30)));
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(defaultConfig)
//                .withInitialCacheConfigurations(cacheConfigs)
//                .build();
//    }
//}


package com.lms.course.config;

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

    // Bean 1: Primary plain ObjectMapper — used by all Services, Controllers, JSON parsing.
    // NO activateDefaultTyping — plain JSON only.
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // Bean 2: Redis-only ObjectMapper — stores class type info for correct deserialization.
    // activateDefaultTyping REQUIRED — without this, Redis cannot deserialize objects
    // after TTL expires, causing silent 500 errors or redirect to login page.
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
                    RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                // cache: prefix — complies with key namespace rules:
                //   rl:*    → API Gateway rate limiting (do not touch)
                //   cache:* → application caching (this service)
                //   auth:*  → reset/verification tokens only
                .prefixCacheNameWith("cache:")
                .entryTtl(Duration.ofMinutes(30));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // ── Course / content caches ───────────────────────────────────────────
        cacheConfigs.put("contentByCourse",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("courseById",
                defaultConfig.entryTtl(Duration.ofMinutes(20)));
        cacheConfigs.put("coursesByEmail",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("coursesByOrg",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("allCategories",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // ── Course feature flag caches ────────────────────────────────────────
        // Feature flags are read on every gated API call via enforce().
        // Caching prevents a DB hit on every request.
        // TTL 30 min — safe because updates evict the cache immediately via @CacheEvict.
        cacheConfigs.put("feature-flags:course:org",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("feature-flags:course:user",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}