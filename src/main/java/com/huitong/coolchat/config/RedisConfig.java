package com.huitong.coolchat.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huitong.coolchat.redis.FastJsonRedisSerializer;
import com.huitong.coolchat.redis.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Autowired
    RedisProperties redisProperties;

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        RedisSerializer redisSerializer = new FastJsonRedisSerializer(Object.class);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        //指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(redisProperties.getMaxActive());
        jedisPoolConfig.setMaxIdle(redisProperties.getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(redisProperties.getMaxWait());
        jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(false);
        jedisPoolConfig.setTestWhileIdle(true);
        JedisClientConfiguration jedisClientConfiguration = null;

        if(redisProperties.getSsl()) {
            jedisClientConfiguration = JedisClientConfiguration.builder()
                    .usePooling()
                    .poolConfig(jedisPoolConfig)
                    .and()
                    .readTimeout(Duration.ofMillis(redisProperties.getConnTimeout()))
                    .useSsl()
                    .build();
        } else {
            jedisClientConfiguration = JedisClientConfiguration.builder()
                    .usePooling()
                    .poolConfig(jedisPoolConfig)
                    .and()
                    .readTimeout(Duration.ofMillis(redisProperties.getConnTimeout()))
                    .build();
        }
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration();
        standaloneConfiguration.setDatabase(redisProperties.getDatabase());
        standaloneConfiguration.setHostName(redisProperties.getHost());
        standaloneConfiguration.setPort(redisProperties.getPort());
        standaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        RedisConnectionFactory connectionFactory = new JedisConnectionFactory(standaloneConfiguration, jedisClientConfiguration);

        return connectionFactory;
    }
}
