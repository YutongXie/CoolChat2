package com.huitong.coolchat.service;

import com.huitong.coolchat.entity.Client;
import com.huitong.coolchat.entity.PurchaseRecord;
import com.huitong.coolchat.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class RushPurchaseService {
    @Autowired
    private RedisTemplate redisTemplate;
    public void purchase(String key) {
//        int number = 10;
        for (int i = 0; i < 1000; i++) {
            int number = new Random().nextInt(10);
            Runnable thread = new RushPurchaseThread(number, "client-" + i, key);
            new Thread(thread).start();
        }
    }

    public void extractPurchase() {
       Set<String> keys = redisTemplate.keys("purchaseRecord-*");
       keys.stream().forEach(key ->{
           log.info("-------Purchase Records -----------");
           log.info("key:" + key);
           List<Client> clientList = redisTemplate.opsForList().range(key, 0, redisTemplate.opsForList().size(key));

           clientList.forEach(client -> {
               log.info("Purchase recrod - client:{}, amount:{}", client.getName(), client.getPurchaseRecordList().get(0).getAmount());
           });

       });
    }

    class RushPurchaseThread implements Runnable {
        private Client client;
        private int number;
        private String key;

        public RushPurchaseThread(int number, String clientName, String key) {
            this.number = number;
            client = new Client();
            client.setName(clientName);
            this.key = key;
        }

        @Override
        public void run() {
            Integer cacheGoods = (Integer)redisTemplate.opsForValue().get(GoodsService.GOOD_KEY + key);
            if(cacheGoods == 0) {
                log.info("Rush Purchase is complete");
                return;
            }
            log.info("goods in redis cache is:{}", cacheGoods);


            long stock = redisTemplate.opsForValue().decrement(GoodsService.GOOD_KEY + key, number);
            if(stock < 0) {
                cacheGoods = (Integer)redisTemplate.opsForValue().get(GoodsService.GOOD_KEY + key);
                if(cacheGoods != 0 && cacheGoods < number) {
                    redisTemplate.opsForValue().increment(GoodsService.GOOD_KEY + key, number);
                    log.info("required number is more than backlog..");
                } else if(cacheGoods == 0) {
                    redisTemplate.opsForValue().set(GoodsService.GOOD_KEY + key, 0);
                }

                log.info("Redis cache has goods:{}", redisTemplate.opsForValue().get(GoodsService.GOOD_KEY + key));
                log.info("failed to purchase the goods. no enough goods");
                return;
            }
            Map params = new HashMap();
            params.put("id", 1);
            params.put("number", number);
            PurchaseRecord purchaseRecord = new PurchaseRecord();
            String id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
            purchaseRecord.setId(System.currentTimeMillis() + "");
            purchaseRecord.setAmount(Double.valueOf(number));
            List<PurchaseRecord> list = new ArrayList<>();
            list.add(purchaseRecord);
            client.setPurchaseRecordList(list);
            redisTemplate.opsForList().leftPush("purchaseRecord-" + id, client);
        }


    }
}
