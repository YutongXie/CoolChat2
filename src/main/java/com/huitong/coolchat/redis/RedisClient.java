package com.huitong.coolchat.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient {
    private JedisPool pool;
    public void initialConnection() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(5);
        config.setMaxTotal(10);
        pool = new JedisPool(config, "127.0.0.1", 6379);
    }

    public Jedis getRedisClient() {
        if(pool == null || pool.isClosed()) {
            initialConnection();
        }
        return pool.getResource();
    }

}
