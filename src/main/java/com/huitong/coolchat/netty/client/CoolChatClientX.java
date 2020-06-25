package com.huitong.coolchat.netty.client;

import com.huitong.coolchat.netty.handler.CoolChatNettyClientHandler;
import com.huitong.coolchat.netty.protocol.CoolChatNettyDecoder;
import com.huitong.coolchat.netty.protocol.CoolChatNettyMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CoolChatClientX {

    public void connect(String hostName, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("decoder", new CoolChatNettyDecoder());
                            socketChannel.pipeline().addLast("encoder", new CoolChatNettyDecoder());
                            socketChannel.pipeline().addLast(new CoolChatNettyClientHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(hostName, port).sync();
            for (int i = 0; i < 50; i++) {
                String message = "Client X message -" + i;
                CoolChatNettyMessage msg = new CoolChatNettyMessage();
                msg.setLength(message.getBytes().length);
                msg.setContent(message.getBytes());
                channelFuture.channel().writeAndFlush(msg);
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        CoolChatClientX client = new CoolChatClientX();
        try {
            client.connect("localhost", 9009);
        } catch (InterruptedException e) {
            log.error("failed to connect to server -", e);
        }
    }
}
