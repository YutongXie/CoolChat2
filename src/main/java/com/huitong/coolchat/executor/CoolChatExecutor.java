package com.huitong.coolchat.executor;

import com.huitong.coolchat.netty.server.CoolChatNettyServer;
import com.huitong.coolchat.redis.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CoolChatExecutor implements ApplicationRunner {
    @Autowired
    CoolChatNettyServer coolChatNettyServer;
    @Autowired
    private GoodsService goodsService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        initialGoods();
        startupNettyServer();

    }

    private void startupNettyServer() throws InterruptedException {
        log.info("Starting up netty server");
        coolChatNettyServer.startUp("localhost", 9009);
        log.info("Netty server is up now..");
    }

    private void initialGoods() {
        log.info("initial goods...");
        goodsService.initialGoods();
        log.info("goods has been intialized on redis");
    }
}
