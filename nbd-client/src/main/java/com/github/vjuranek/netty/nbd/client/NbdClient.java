package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;

public final class NbdClient {

    static final String HOST = "127.0.0.1";

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new NbdClientHandler());
                        }
                    });

            b.remoteAddress(HOST, Constants.NBD_PORT);
            ChannelFuture f = b.connect().sync();

            f.channel().closeFuture().sync();
        } finally {
            Future<?> f = group.shutdownGracefully();
            f.syncUninterruptibly();
        }
    }
}