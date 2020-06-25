package com.huitong.coolchat.netty.handler;

import com.huitong.coolchat.netty.protocol.CoolChatNettyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoolChatNettyClientHandler extends SimpleChannelInboundHandler<CoolChatNettyMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CoolChatNettyMessage msg) throws Exception {
        log.info(new String(msg.getContent(), CharsetUtil.UTF_8));
    }
}
