package com.huitong.coolchat.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoolChatNettyEncoder extends MessageToByteEncoder<CoolChatNettyMessage> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, CoolChatNettyMessage coolChatProtocol, ByteBuf byteBuf) throws Exception {
        int length = coolChatProtocol.getLength();
        byte[] content = coolChatProtocol.getContent();
        log.info("start to encode-length:{}, content:{}", length, new String(content, CharsetUtil.UTF_8));
        byteBuf.writeInt(length);
        byteBuf.writeBytes(content);
    }
}
