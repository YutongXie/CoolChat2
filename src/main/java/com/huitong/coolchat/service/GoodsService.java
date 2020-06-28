package com.huitong.coolchat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class GoodsService {
    public static final String GOOD_KEY = "goods:";

    @Autowired
    private RedisTemplate redisTemplate;

    public void initialGoods() {
        redisTemplate.opsForValue().set(GOOD_KEY + 1, 100);
    }
}
