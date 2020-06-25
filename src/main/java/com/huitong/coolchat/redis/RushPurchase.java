package com.huitong.coolchat.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Component
@Slf4j
public class RushPurchase {
    @Autowired
    private RedisTemplate redisTemplate;
    public void purchase() {
        int number = 10;
        for (int i = 0; i < 1000; i++) {
            Runnable thread = new RushPurchaseThread(number);
            new Thread(thread).start();
        }
    }

    class RushPurchaseThread implements Runnable {
        private int number;

        public RushPurchaseThread(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            Integer cacheGoods = (Integer)redisTemplate.opsForValue().get(GoodsService.GOOD_KEY + 1);
            if(cacheGoods == 0) {
                log.info("Rush Purchase is complete");
                return;
            }
            log.info("goods in redis cache is:{}", cacheGoods);


            long stock = redisTemplate.opsForValue().decrement(GoodsService.GOOD_KEY + 1, number);
            if(stock < 0) {
                cacheGoods = (Integer)redisTemplate.opsForValue().get(GoodsService.GOOD_KEY + 1);
                if(cacheGoods != 0 && cacheGoods < number) {
                    redisTemplate.opsForValue().increment(GoodsService.GOOD_KEY + 1, number);
                    log.info("required number is more than backlog..");
                } else if(cacheGoods == 0) {
                    redisTemplate.opsForValue().set(GoodsService.GOOD_KEY + 1, 0);
                }

                log.info("Redis cache has goods:{}", redisTemplate.opsForValue().get(GoodsService.GOOD_KEY + 1));
                log.info("failed to purchase the goods. no enough goods");
                return;
            }
            Map params = new HashMap();
            params.put("id", 1);
            params.put("number", number);

        }
    }
}
