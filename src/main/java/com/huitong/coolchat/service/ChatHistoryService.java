package com.huitong.coolchat.service;

import com.huitong.coolchat.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ChatHistoryService {
    @Autowired
    private RedisTemplate redisTemplate;
    private RedisClient redisClient = new RedisClient();
    public void recordMessage(String msg) {
        redisTemplate.opsForList().leftPush("chatMsg-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")), msg);
    }

    public void extractAllMessage() {
        redisTemplate.keys("chatMsg-*");
        Jedis jedis = redisClient.getRedisClient();
//        Set<String> keys = jedis.keys("chatMsg-*");
        Set<String> keys = redisTemplate.keys("chatMsg-*");
        keys.stream().forEach(key ->{
            log.info("----");
            log.info(key);
//            List<String> msgHistory = jedis.lrange(key, 0, jedis.llen(key));
            List<String> msgHistory = redisTemplate.opsForList().range(key, 0, redisTemplate.opsForList().size(key));
            msgHistory.forEach(msg -> {
                log.info(msg);
            });
            log.info("----");
        });




    }
}
