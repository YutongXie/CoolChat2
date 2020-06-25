package com.huitong.coolchat.netty.server;

import com.huitong.coolchat.netty.handler.CoolChatNettyServerHandler;
import com.huitong.coolchat.netty.protocol.CoolChatNettyDecoder;
import com.huitong.coolchat.netty.protocol.CoolChatNettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoolChatNettyServer {
    @Autowired
    private CoolChatNettyServerHandler coolChatNettyServerHandler;
    public void startUp(String hostName, int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("encoder", new CoolChatNettyEncoder());
                            socketChannel.pipeline().addLast("decoder", new CoolChatNettyDecoder());
                            socketChannel.pipeline().addLast(coolChatNettyServerHandler);
                        }
                    });

            ChannelFuture future = bootstrap.bind(hostName, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        CoolChatNettyServer server = new CoolChatNettyServer();
        try {
            server.startUp("localhost", 9009);
        } catch (InterruptedException e) {
            log.error("failed to start up server-", e);
        }
    }
}
