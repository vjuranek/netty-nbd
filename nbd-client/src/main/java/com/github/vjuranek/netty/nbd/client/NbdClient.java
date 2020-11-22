package com.github.vjuranek.netty.nbd.client;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

public final class NbdClient {

    static final String HOST = "127.0.0.1";
    private static final EventLoopGroup group = new NioEventLoopGroup();

    private final Channel channel;

    public NbdClient() throws  InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                //.option(ChannelOption.AUTO_READ, false)
                .handler(new NbdChannelInitializer());

        b.remoteAddress(HOST, Constants.NBD_PORT);
        ChannelFuture f = b.connect().sync();

        this.channel = f.channel();
    }

    private byte[] sendOption(int option, byte[] data) throws InterruptedException {
        OptionHandler handler = new OptionHandler(option);
        channel.pipeline().addLast(handler);

        int len = 16 + data.length; // long + int + int + data length
        ByteBuf b = Unpooled.buffer(len);
        b.writeLong(Constants.I_HAVE_OPT);
        b.writeInt(option);
        b.writeInt(data.length);
        if (data.length > 0) {
            b.writeBytes(data);
        }

        ChannelFuture f = channel.pipeline().writeAndFlush(b);
        f.addListener(Utils.writeFailed);
        f.sync();

        return handler.getReply();
    }

    public static void main(String[] args) throws Exception {
        NbdClient client;
        try {
            client = new NbdClient();
            byte[] reply = client.sendOption(Constants.NBD_OPT_STRUCTURED_REPLY, new byte[] {});
            System.out.println("REPLY: " + new String(reply));
        } catch(InterruptedException e) {
            shutdown();
        }

    }

    private static void shutdown() {
        Future<?> f = group.shutdownGracefully();
        f.syncUninterruptibly();
    }
}