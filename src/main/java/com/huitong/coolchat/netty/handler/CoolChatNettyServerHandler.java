package com.huitong.coolchat.netty.handler;

import com.huitong.coolchat.netty.protocol.CoolChatNettyMessage;
import com.huitong.coolchat.service.RushPurchaseService;
import com.huitong.coolchat.service.ChatHistoryService;
import com.huitong.coolchat.service.GoodsService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@ChannelHandler.Sharable
public class CoolChatNettyServerHandler extends SimpleChannelInboundHandler<CoolChatNettyMessage> {
    @Autowired
    private ChatHistoryService chatHistoryService;
    @Autowired
    private RushPurchaseService rushPurchase;
    @Autowired
    private GoodsService goodsService;

    private static final ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CoolChatNettyMessage msg) throws Exception {
        Channel fromChannel = channelHandlerContext.channel();
        int length = msg.getLength();
        String content = new String(msg.getContent(), CharsetUtil.UTF_8);
        log.info("Server received message from client- length:{}, content:{}", length, content);
        chatHistoryService.recordMessage("[From" + fromChannel.remoteAddress() + "]-" + content);
        group.forEach(channel -> {
            if(channel == fromChannel) {
                CoolChatNettyMessage newMsg = new CoolChatNettyMessage();
                newMsg.setContent(("[From me]-" + content).getBytes());
                newMsg.setLength(("[From me]-" + content).getBytes().length);
                channel.pipeline().writeAndFlush(newMsg);
            } else {
                CoolChatNettyMessage newMsg = new CoolChatNettyMessage();
                newMsg.setContent(("[From" + channel.remoteAddress() + "]-" + content).getBytes());
                newMsg.setLength(("[From" + channel.remoteAddress() + "]-" + content).getBytes().length);
                channel.pipeline().writeAndFlush(newMsg);
            }
        });

        if("extract".equalsIgnoreCase(content)) {
            chatHistoryService.extractAllMessage();
        }

        if("extractPurchase".equalsIgnoreCase(content)) {
            rushPurchase.extractPurchase();
        }

        if(content.startsWith("rushpurchase")) {
            int amountIndex = content.indexOf(" ");
            String amountStr = StringUtils.trimToEmpty(content.substring(amountIndex));
            if(StringUtils.isBlank(amountStr))
                return;
            int amount = Integer.valueOf(amountStr);
            String key = System.currentTimeMillis() +"";
            goodsService.setupGoods(key, amount);

            group.forEach(channel ->{
                for (int i = 10; i > 0; i--) {
                    CoolChatNettyMessage newMsg = new CoolChatNettyMessage();
                    newMsg.setContent(("[From System]-" + i).getBytes());
                    newMsg.setLength(("[From System]-" + i).getBytes().length);
                    channel.pipeline().writeAndFlush(newMsg);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.info("sleeping 1 sec failed");
                    }
                }
            });
            rushPurchase.purchase(key);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel newChannel = ctx.channel();
        group.forEach(channel -> {
            String msg = "Client " + newChannel.remoteAddress() + " is online at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:SS"));
            CoolChatNettyMessage newMsg = new CoolChatNettyMessage();
            newMsg.setContent(msg.getBytes());
            newMsg.setLength(msg.getBytes().length);
            channel.pipeline().writeAndFlush(newMsg);
        });
        group.add(newChannel);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel quitChannel = ctx.channel();
        group.forEach(channel -> {
            String msg = "Client " + quitChannel.remoteAddress() + " is offline at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:SS"));
            CoolChatNettyMessage newMsg = new CoolChatNettyMessage();
            newMsg.setContent(msg.getBytes());
            newMsg.setLength(msg.getBytes().length);
            channel.pipeline().writeAndFlush(newMsg);
        });
        group.remove(quitChannel);
    }

}
