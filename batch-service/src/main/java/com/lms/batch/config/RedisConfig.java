
package com.lms.batch.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    // Namespaced prefix so this service's cache keys can never collide with
    // auth-service / user-service keys, even on the same Redis instance/DB.
    private static final String CACHE_PREFIX = "cache:batch:";

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ✅ FIXED — added JsonTypeInfo.As.PROPERTY as the third argument.
        //
        // The old call (no third arg) defaulted to JsonTypeInfo.As.WRAPPER_ARRAY,
        // which wraps every non-final object as ["ClassName", {...}]. That's fine
        // for a single object, but breaks the moment you cache a List<T>: the list
        // itself stays a plain JSON array (lists don't need a type wrapper), while
        // each element inside it gets individually wrapped — producing
        // [["com.lms.batch.dto.BatchResponseDTO", {...}]].
        //
        // On read, deserialize() calls mapper.readValue(bytes, Object.class).
        // Reading into the bare Object.class is itself polymorphic, so with
        // WRAPPER_ARRAY typing Jackson expects the FIRST element of the top-level
        // array to be a type-id string (to know what single object to rebuild).
        // Instead it finds another array — hence "Unexpected token (START_ARRAY),
        // expected VALUE_STRING ... need String/Number/Boolean value that
        // contains type id".
        //
        // PROPERTY-based typing avoids this entirely: instead of wrapping objects
        // in an array, it embeds a hidden "@class" field inside each JSON object.
        // Lists/arrays keep their normal JSON shape with no ambiguity, regardless
        // of how many objects are inside them.
        mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();

        return template;
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
                .prefixCacheNameWith(CACHE_PREFIX)
                .entryTtl(Duration.ofMinutes(10));

        // ✅ Cache names reverted to their real (non-":v2") names so they
        // actually match whatever @Cacheable(value = "...") names your
        // service methods use. The previous ":v2" bump only matters if your
        // @Cacheable annotations were ALSO updated to use ":v2" — if they
        // weren't, these entries in the map were never being hit at all,
        // and every cache fell through to cacheDefaults() instead (which,
        // luckily, uses the same serializer, so behavior was equivalent,
        // just without the per-cache TTL tuning below).
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("batches:org",            defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("batches:trainer",         defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("student:batch",           defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("student:classroom",       defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("batch:trainer-students",  defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("feature-flags:org",       defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("feature-flags:user",      defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("org:summary",             defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    // Without this, any future cache-read failure (serializer mismatch,
    // corrupted bytes, DTO shape change) throws all the way up through
    // @Cacheable and 500s the controller — exactly what happened before.
    // With this handler, a cache-get failure is logged and treated as a
    // miss, so the method just falls through to the DB and the request
    // still succeeds.
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                System.err.println("⚠️ Redis cache GET failed for cache="
                        + cache.getName() + " key=" + key + " — falling back to source. " + e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                System.err.println("⚠️ Redis cache PUT failed for cache="
                        + cache.getName() + " key=" + key + ": " + e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                System.err.println("⚠️ Redis cache EVICT failed for cache="
                        + cache.getName() + " key=" + key + ": " + e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                System.err.println("⚠️ Redis cache CLEAR failed for cache="
                        + cache.getName() + ": " + e.getMessage());
            }
        };
    }

    @Bean
    public CachingConfigurer cachingConfigurer(CacheErrorHandler cacheErrorHandler) {
        return new CachingConfigurer() {
            @Override
            public CacheErrorHandler errorHandler() {
                return cacheErrorHandler;
            }
        };
    }
}