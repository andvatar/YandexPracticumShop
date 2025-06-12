package ru.yandex.practicum.tarasov.yandexpracticumshop.configuration;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
@EnableCaching
public class RedisCacheConfig {
    private final CacheProperties cacheProperties;

    public RedisCacheConfig(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {

        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.of(cacheProperties.getRedis().getTimeToLive().get(ChronoUnit.SECONDS), ChronoUnit.SECONDS))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
